package com.devgrapher.ocebook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.devgrapher.ocebook.model.OpenPageRequest;
import com.devgrapher.ocebook.model.Page;
import com.devgrapher.ocebook.model.PaginationInfo;
import com.devgrapher.ocebook.readium.ObjectHolder;
import com.devgrapher.ocebook.readium.ReadiumContext;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.SpineItem;

import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HiddenRendererFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HiddenRendererFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HiddenRendererFragment extends WebViewFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = WebViewFragment.class.toString();
    private static final String ARG_CONTAINER_ID = "container";

    private OnFragmentInteractionListener mListener;

    public HiddenRendererFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param containerId container id held by ObjectHolder.
     * @return A new instance of fragment HiddenRendererFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HiddenRendererFragment newInstance(long containerId) {
        HiddenRendererFragment fragment = new HiddenRendererFragment();

        Bundle args = new Bundle();
        args.putLong(ARG_CONTAINER_ID, containerId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        WebView webView = (WebView) view.findViewById(R.id.webView);
        webView.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onBrowsePageInProgress(int currentSpine, int totalSpine, int pageCount);
    }

    @Override
    public ReadiumContext.PageEventListener createPageEventListener() {
        return new ReadiumContext.PageEventListener() {

            @Override
            public void onReaderInitialized() {
                getActivity().runOnUiThread(() -> {
                    final Package pkcg = mReadiumCtx.getPackage();
                    pkcg.getSpineItems().stream()
                            .findAny()
                            .ifPresent(item -> {
                                mReadiumCtx.getApi().openBook(pkcg, mViewerSettings,
                                        OpenPageRequest.fromIdref(item.getIdRef()));
                            });
                });
            }

            @Override
            public void onPaginationChanged(PaginationInfo currentPagesInfo) {
                Log.d(TAG, "onPaginationChanged: " + currentPagesInfo);
                List<Page> openPages = currentPagesInfo.getOpenPages();
                if (openPages.isEmpty())
                    return;

                getActivity().runOnUiThread(() -> {
                    final Page page = openPages.get(0);

                    int totalSpine = currentPagesInfo.getSpineItemCount();
                    int spineIdx = page.getSpineItemIndex();
                    int nextSpine = spineIdx + 1;

                    // notify the page count in current spine.
                    mListener.onBrowsePageInProgress(
                            spineIdx, totalSpine, page.getSpineItemPageCount());

                    if (nextSpine < totalSpine) {
                        // open next spine
                        Log.i(TAG, "Browse spines" + nextSpine + "/" + totalSpine);

                        SpineItem spine = mReadiumCtx.getPackage().getSpineItems().get(nextSpine);
                        mReadiumCtx.getApi().openBook(mReadiumCtx.getPackage(), mViewerSettings,
                                OpenPageRequest.fromIdref(spine.getIdRef()));
                    }
                });
            }
        };
    }
}
