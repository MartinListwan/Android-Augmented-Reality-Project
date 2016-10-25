/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.Books.app.Books;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vuforia.CameraDevice;
import com.vuforia.ObjectTracker;
import com.vuforia.State;
import com.vuforia.TargetFinder;
import com.vuforia.TargetSearchResult;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;
import com.vuforia.samples.Books.R;
import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.SampleApplication.SampleApplicationException;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.vuforia.samples.SampleApplication.utils.Texture;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


// The main activity for the Books sample.
public class Books extends Activity implements SampleApplicationControl
{
    private static final String LOGTAG = "Books";

    private static HashMap<Integer,HashMap<Integer,List<String>>> hashItemToStateToList = new HashMap<Integer,HashMap<Integer,List<String>>>();

    SampleApplicationSession vuforiaAppSession;

    static int count = 0;
    // Defines the Server URL to get the books data
    private static final String mServerURL = "https://developer.vuforia.com/samples/cloudreco/json/";

    // Stores the current status of the target ( if is being displayed or not )
    private static final int BOOKINFO_NOT_DISPLAYED = 0;
    private static final int BOOKINFO_IS_DISPLAYED = 1;

    // These codes match the ones defined in TargetFinder in Vuforia.jar
    static final int INIT_SUCCESS = 2;
    static final int INIT_ERROR_NO_NETWORK_CONNECTION = -1;
    static final int INIT_ERROR_SERVICE_NOT_AVAILABLE = -2;
    static final int UPDATE_ERROR_AUTHORIZATION_FAILED = -1;
    static final int UPDATE_ERROR_PROJECT_SUSPENDED = -2;
    static final int UPDATE_ERROR_NO_NETWORK_CONNECTION = -3;
    static final int UPDATE_ERROR_SERVICE_NOT_AVAILABLE = -4;
    static final int UPDATE_ERROR_BAD_FRAME_QUALITY = -5;
    static final int UPDATE_ERROR_UPDATE_SDK = -6;
    static final int UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7;
    static final int UPDATE_ERROR_REQUEST_TIMEOUT = -8;

    // Handles Codes to display/Hide views
    static final int HIDE_STATUS_BAR = 0;
    static final int SHOW_STATUS_BAR = 1;

    static final int HIDE_2D_OVERLAY = 0;
    static final int SHOW_2D_OVERLAY = 1;

    static final int HIDE_LOADING_DIALOG = 0;
    static final int SHOW_LOADING_DIALOG = 1;

    // Augmented content status
    private int mBookInfoStatus = BOOKINFO_NOT_DISPLAYED;

    // Status Bar Text
    private String mStatusBarText;

    // Active Book Data
    private Book mBookData;
    private String mBookJSONUrl;
    private Texture mBookDataTexture;

    // Indicates if the app is currently loading the book data
    private boolean mIsLoadingBookData = false;

    // AsyncTask to get book data from a json object
    private GetBookDataTask mGetBookDataTask;

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private BooksRenderer mRenderer;

    private static final String kAccessKey = "31da7d9f19dfaa540932ff51b481cccd9f681e30";
    private static final String kSecretKey = "50289d4420a7dbf035b40caa87253c57b2f250f9";

    // View overlays to be displayed in the Augmented View
    private RelativeLayout mUILayout;
    private TextView mStatusBar;
    private Button mCloseButton;

    // Error message handling:
    private int mlastErrorCode = 0;
    private int mInitErrorCode = 0;
    private boolean mFinishActivityOnError;

    public static final String a001 = "visible";
    public static final String a002 = "http://cdn-wov.anitaborg.org/wp-content/uploads/sites/4/2016/03/alyssia-jovellanos-700x467.jpg";
    public static final String a003 = "Small Business";
    public static final String a004 = "Alyssia Lmt";
    public static final String a005 = "Owner: Alyssia Jovellanos";

    public static final String a006 = "visible";
    public static final String a007 = "Account Balance: $5201.03";
    public static final String a008 = "";
    public static final String a009 = "(!) 2 cheques scheduled to cash this month";
    public static final String a0010 = "(!) 1 outstanding invoice, 29 days left to pay";
    public static final String a0011 = "";
    public static final String a0012 = "";
    public static final String a0013 = "";
    public static final String a0014 = "";

    public static final String b001 = "";
    public static final String b002 = "";
    public static final String b003 = "";
    public static final String b004 = "";
    public static final String b005 = "";


    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    // Detects the double tap gesture for launching the Camera menu
    private GestureDetector mGestureDetector;

    private String lastTargetId = "";

    // size of the Texture to be generated with the book data
    private static int mTextureSize = 768;

    // declare scan line and its animation
    private View scanLine;
    private TranslateAnimation scanAnimation;
    private boolean scanLineStarted;

    private static List<String> metaData;

    public void deinitBooks()
    {
        // Get the object tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
        {
            Log.e(LOGTAG,
                    "Failed to destroy the tracking data set because the ObjectTracker has not"
                            + " been initialized.");
            return;
        }

        // Deinitialize target finder:
        TargetFinder finder = objectTracker.getTargetFinder();
        finder.deinit();
    }


    private void initStateVariables()
    {
        mRenderer.setRenderState(BooksRenderer.RS_SCANNING);
        mRenderer.setProductTexture(null);

        mRenderer.setScanningMode(true);
        mRenderer.isShowing2DOverlay(false);
        mRenderer.showAnimation3Dto2D(false);
        mRenderer.stopTransition3Dto2D();
        mRenderer.stopTransition2Dto3D();

        cleanTargetTrackedId();
    }




    private void inithashItemToStateToList(){
        Toast.makeText(getApplicationContext(),"Made info",Toast.LENGTH_SHORT ).show();
        HashMap<Integer,List<String>> hs = new HashMap<Integer,List<String>>();
        // selection 0, for state 0 which will be the original alyssia


        List<String> list = new ArrayList<String>();
        list.add("visible");
        list.add("http://cdn-wov.anitaborg.org/wp-content/uploads/sites/4/2016/03/alyssia-jovellanos-700x467.jpg");
        list.add("Small Business");
        list.add("Alyssia Lmt");
        list.add("Owner: Alyssia Jovellanos");

        // second linear
        list.add("visible");
        list.add("Account Balance: $5201.03");
        list.add("");
        list.add("3 charges left to cash Sept 24th");
        list.add("2 outstanding invoices");
        list.add("");
        list.add("");
        list.add("");
        hs.put(0,list);
        list.clear();

        // selection 0, state 1
        list.add("");
        list.add("");
        list.add("");
        list.add("");
        list.add("");

        // Second linear
        list.add("visible");
        list.add("Your companies personal data");
        // change this so that it has the url of the pie chart
        list.add("http://untagged.co.uk/images/graph-stats-green.png");
        list.add("");
        list.add("");
        list.add("");
        list.add("");
        list.add("");
        hs.put(1,list);
        list.clear();

        hashItemToStateToList.put(0,hs);
    }

    /**
     * Function to generate the OpenGL Texture Object in the renderFrame thread
     */
    public void productTextureIsCreated()
    {
        mRenderer.setRenderState(BooksRenderer.RS_TEXTURE_GENERATED);
    }


    /** Sets current device Scale factor based on screen dpi */
    public void setDeviceDPIScaleFactor(float dpiSIndicator)
    {
        mRenderer.setDPIScaleIndicator(dpiSIndicator);

        // MDPI devices
        if (dpiSIndicator <= 1.0f)
        {
            mRenderer.setScaleFactor(1.6f);
        }
        // HDPI devices
        else if (dpiSIndicator <= 1.5f)
        {
            mRenderer.setScaleFactor(1.3f);
        }
        // XHDPI devices
        else if (dpiSIndicator <= 2.0f)
        {
            mRenderer.setScaleFactor(1.0f);
        }
        // XXHDPI devices
        else
        {
            mRenderer.setScaleFactor(0.6f);
        }
    }


    /** Cleans the lastTargetTrackerId variable */
    public void cleanTargetTrackedId()
    {
        synchronized (lastTargetId)
        {
            lastTargetId = "";
        }
    }

    /**
     * Crates a Handler to Show/Hide the status bar overlay from an UI Thread
     */
    static class StatusBarHandler extends Handler
    {
        private final WeakReference<Books> mBooks;


        StatusBarHandler(Books books)
        {
            mBooks = new WeakReference<Books>(books);
        }


        public void handleMessage(Message msg)
        {
            Books books = mBooks.get();
            if (books == null)
            {
                return;
            }

            if (msg.what == SHOW_STATUS_BAR)
            {
                books.mStatusBar.setText(books.mStatusBarText);
                books.mStatusBar.setVisibility(View.VISIBLE);
            } else
            {
                books.mStatusBar.setVisibility(View.GONE);
            }
        }
    }

    private Handler statusBarHandler = new StatusBarHandler(this);

    /**
     * Creates a handler to Show/Hide the UI Overlay from an UI thread
     */
    static class Overlay2dHandler extends Handler
    {
        private final WeakReference<Books> mBooks;


        Overlay2dHandler(Books books)
        {
            mBooks = new WeakReference<Books>(books);
        }


        public void handleMessage(Message msg)
        {
            Books books = mBooks.get();
            if (books == null)
            {
                return;
            }

            if (books.mCloseButton != null)
            {
                if (msg.what == SHOW_2D_OVERLAY)
                {
                    books.mCloseButton.setVisibility(View.VISIBLE);
                } else
                {
                    books.mCloseButton.setVisibility(View.GONE);
                }
            }
        }
    }

    private Handler overlay2DHandler = new Overlay2dHandler(this);

    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
            this);

    private double mLastErrorTime;

    private float mdpiScaleIndicator;

    boolean mIsDroidDevice = false;

    private Activity mActivity = null;


    // Called when the activity first starts or needs to be recreated after
    // resuming the application or a configuration change.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);

        inithashItemToStateToList();
        mActivity = this;

        vuforiaAppSession = new SampleApplicationSession(this);

        startLoadingAnimation();

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        // Creates the GestureDetector listener for processing double tap
        mGestureDetector = new GestureDetector(this, new GestureListener());

        mdpiScaleIndicator = getApplicationContext().getResources()
                .getDisplayMetrics().density;

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
                "droid");
    }


    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

        mBookInfoStatus = BOOKINFO_NOT_DISPLAYED;

        // By default the 2D Overlay is hidden
        hide2DOverlay();
    }


    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
        scanCreateAnimation();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // When the camera stops it clears the Product Texture ID so next time
        // textures
        // Are recreated
        if (mRenderer != null)
        {
            mRenderer.deleteCurrentProductTexture();

            // Initialize all state Variables
            initStateVariables();
        }

        // Pauses the OpenGLView
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        System.gc();
    }


    private void startLoadingAnimation()
    {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(
                R.layout.camera_overlay_books, null, false);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // By default
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_layout);
        loadingDialogHandler.mLoadingDialogContainer
                .setVisibility(View.VISIBLE);

        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        // Gets a Reference to the Bottom Status Bar
        mStatusBar = (TextView) mUILayout.findViewById(R.id.overlay_status);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Gets a reference to the Close Button
        mCloseButton = (Button) mUILayout
                .findViewById(R.id.overlay_close_button);

        // Sets the Close Button functionality
        mCloseButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                // Updates application status
                mBookInfoStatus = BOOKINFO_NOT_DISPLAYED;

                loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

                // Checks if the app is currently loading a book data
                if (mIsLoadingBookData)
                {

                    // Cancels the AsyncTask
                    mGetBookDataTask.cancel(true);
                    mIsLoadingBookData = false;

                    // Cleans the Target Tracker Id
                    cleanTargetTrackedId();
                }

                // Enters Scanning Mode
                enterScanningMode();
            }
        });

        // As default the 2D overlay and Status bar are hidden when application
        // starts
        hide2DOverlay();
        hideStatusBar();

        scanLine = mUILayout.findViewById(R.id.scan_line);
        scanLine.setVisibility(View.GONE);
        scanLineStarted = false;
        scanCreateAnimation();
    }


    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        // Initialize the GLView with proper flags
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        // Setups the Renderer of the GLView
        mRenderer = new BooksRenderer(vuforiaAppSession);
        mRenderer.mActivity = this;
        mGlView.setRenderer(mRenderer);

        // Sets the device scale density
        setDeviceDPIScaleFactor(mdpiScaleIndicator);

        initStateVariables();
    }


    /** Sets the Status Bar Text in a UI thread */
    public void setStatusBarText(String statusText)
    {
        mStatusBarText = statusText;
        statusBarHandler.sendEmptyMessage(SHOW_STATUS_BAR);
    }


    /** Hides the Status bar 2D Overlay in a UI thread */
    public void hideStatusBar()
    {
        if (mStatusBar.getVisibility() == View.VISIBLE)
        {
            statusBarHandler.sendEmptyMessage(HIDE_STATUS_BAR);
        }
    }


    /** Shows the Status Bar 2D Overlay in a UI thread */
    public void showStatusBar()
    {
        if (mStatusBar.getVisibility() == View.GONE)
        {
            statusBarHandler.sendEmptyMessage(SHOW_STATUS_BAR);
        }
    }


    public void startWebView(int value)
    {
        // Checks that we have a valid book data
        if (mBookData != null)
        {
            // Starts an Intent to open the book URL
            Intent viewIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse(mBookData.getBookUrl()));

            startActivity(viewIntent);
        }
    }


    /** Returns the error message for each error code */
    private String getStatusDescString(int code)
    {
        if (code == UPDATE_ERROR_AUTHORIZATION_FAILED)
            return getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_DESC);
        if (code == UPDATE_ERROR_PROJECT_SUSPENDED)
            return getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_DESC);
        if (code == UPDATE_ERROR_NO_NETWORK_CONNECTION)
            return getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_DESC);
        if (code == UPDATE_ERROR_SERVICE_NOT_AVAILABLE)
            return getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_DESC);
        if (code == UPDATE_ERROR_UPDATE_SDK)
            return getString(R.string.UPDATE_ERROR_UPDATE_SDK_DESC);
        if (code == UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE)
            return getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_DESC);
        if (code == UPDATE_ERROR_REQUEST_TIMEOUT)
            return getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_DESC);
        if (code == UPDATE_ERROR_BAD_FRAME_QUALITY)
            return getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_DESC);
        else
        {
            return getString(R.string.UPDATE_ERROR_UNKNOWN_DESC);
        }
    }


    /** Returns the error message for each error code */
    private String getStatusTitleString(int code)
    {
        if (code == UPDATE_ERROR_AUTHORIZATION_FAILED)
            return getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_TITLE);
        if (code == UPDATE_ERROR_PROJECT_SUSPENDED)
            return getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_TITLE);
        if (code == UPDATE_ERROR_NO_NETWORK_CONNECTION)
            return getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_TITLE);
        if (code == UPDATE_ERROR_SERVICE_NOT_AVAILABLE)
            return getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_TITLE);
        if (code == UPDATE_ERROR_UPDATE_SDK)
            return getString(R.string.UPDATE_ERROR_UPDATE_SDK_TITLE);
        if (code == UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE)
            return getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_TITLE);
        if (code == UPDATE_ERROR_REQUEST_TIMEOUT)
            return getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_TITLE);
        if (code == UPDATE_ERROR_BAD_FRAME_QUALITY)
            return getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_TITLE);
        else
        {
            return getString(R.string.UPDATE_ERROR_UNKNOWN_TITLE);
        }
    }


    // Shows error messages as System dialogs
    public void showErrorMessage(int errorCode, double errorTime, boolean finishActivityOnError)
    {
        if (errorTime < (mLastErrorTime + 5.0) || errorCode == mlastErrorCode)
            return;

        mlastErrorCode = errorCode;
        mFinishActivityOnError = finishActivityOnError;

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        Books.this);
                builder
                        .setMessage(
                                getStatusDescString(Books.this.mlastErrorCode))
                        .setTitle(
                                getStatusTitleString(Books.this.mlastErrorCode))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        if(mFinishActivityOnError)
                                        {
                                            finish();
                                        }
                                        else
                                        {
                                            dialog.dismiss();
                                        }
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    /**
     * Generates a texture for the book data fetching the book info from the
     * specified book URL
     */
    public void createProductTexture(String bookJSONUrl)
    {
        // gets book url from parameters
        mBookJSONUrl = bookJSONUrl.trim();

        // Cleans old texture reference if necessary
        if (mBookDataTexture != null)
        {
            mBookDataTexture = null;

            System.gc();
        }

        // Searches for the book data in an AsyncTask
        mGetBookDataTask = new GetBookDataTask();
        mGetBookDataTask.execute();
    }

    /** Gets the book data from a JSON Object */
    private class GetBookDataTask extends AsyncTask<Void, Void, Void>
    {
        private String mBookDataJSONFullUrl;
        private static final String CHARSET = "UTF-8";


        protected void onPreExecute()
        {
            mIsLoadingBookData = true;

            // Initialize the current book full url to search
            // for the data
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append(mServerURL);
            sBuilder.append(mBookJSONUrl);

            mBookDataJSONFullUrl = sBuilder.toString();

            // Shows the loading dialog
            loadingDialogHandler.sendEmptyMessage(SHOW_LOADING_DIALOG);
        }


        protected Void doInBackground(Void... params)
        {
            HttpURLConnection connection = null;

            try
            {
                // Connects to the Server to get the book data
                URL url = new URL(mBookDataJSONFullUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Charset", CHARSET);
                connection.connect();

                int status = connection.getResponseCode();

                // Checks that the book JSON url exists and connection
                // has been successful
                if (status != HttpURLConnection.HTTP_OK)
                {
                    // Cleans book data variables
                    mBookData = null;
                    mBookInfoStatus = BOOKINFO_NOT_DISPLAYED;

                    // Hides loading dialog
                    loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

                    // Cleans current tracker Id and returns to scanning mode
                    cleanTargetTrackedId();

                    enterScanningMode();
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    builder.append(line);
                }

                // Cleans any old reference to mBookData
                if (mBookData != null)
                {
                    mBookData = null;

                }

                JSONObject jsonObject = new JSONObject(builder.toString());

                // Generates a new Book Object with the JSON object data
                mBookData = new Book();


                byte[] thumb;
                if (count == 0){
                    mBookData.setTitle("Small Business");
                    mBookData.setAuthor("Owner of Fun E");

                    mBookData.setBookUrl("http://www.rbc.com/canada.html");
                    mBookData.setPriceList("");
                    Log.d("MARTIN",jsonObject.toString());
                    mBookData.setPriceYour("211");
                    mBookData.setRatingAvg("4f");
                    mBookData.setRatingTotal("12");
                    thumb = downloadImage("http://cdn-wov.anitaborg.org/wp-content/uploads/sites/4/2016/03/alyssia-jovellanos-700x467.jpg");
                } else {
                    mBookData.setTitle("Can Of Coke");
                    mBookData.setAuthor("Invetory: 63");

                    mBookData.setBookUrl("https://mycokedsdol.force.com/DefaultStore/");
                    mBookData.setPriceList("");
                    Log.d("MARTIN",jsonObject.toString());
                    mBookData.setPriceYour("211");
                    mBookData.setRatingAvg("4f");
                    mBookData.setRatingTotal("12");
                    thumb = downloadImage("http://livenhub.com/wp-content/uploads/2015/07/cocacola.jpg");
                }


                // Gets the book thumb image
                //byte[] thumb = downloadImage(jsonObject.getString("thumburl"));
                //http://vignette1.wikia.nocookie.net/simpsonstappedout/images/a/ab/Homer-simpson.jpg/revision/latest?cb=20160121173741.jpg

                if (thumb != null)
                {

                    Bitmap bitmap = BitmapFactory.decodeByteArray(thumb, 0,
                            thumb.length);
                    mBookData.setThumb(bitmap);
                }
            } catch (Exception e)
            {
                Log.d(LOGTAG, "Couldn't get books. e: " + e);
            } finally
            {
                connection.disconnect();
            }

            return null;
        }


        protected void onProgressUpdate(Void... values)
        {

        }


        protected void onPostExecute(Void result)
        {
            // THis is where i need to highjack for the first download
            if (mBookData != null)
            {
                // Generates a View to display the book data
                BookOverlayView productView = new BookOverlayView(
                        Books.this);

                // Updates the view used as a 3d Texture
                // update edit this needs to change object number to 1 for non alyssia card
                updateProductView(productView, mBookData,0,0);

                // Sets the layout params
                productView.setLayoutParams(new LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT));

                // Sets View measure - This size should be the same as the
                // texture generated to display the overlay in order for the
                // texture to be centered in screen
                productView.measure(MeasureSpec.makeMeasureSpec(mTextureSize,
                        MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                        mTextureSize, MeasureSpec.EXACTLY));

                // updates layout size
                productView.layout(0, 0, productView.getMeasuredWidth(),
                        productView.getMeasuredHeight());

                // Draws the View into a Bitmap. Note we are allocating several
                // large memory buffers thus attempt to clear them as soon as
                // they are no longer required:
                Bitmap bitmap = Bitmap.createBitmap(mTextureSize, mTextureSize,
                        Bitmap.Config.ARGB_8888);

                Canvas c = new Canvas(bitmap);
                productView.draw(c);

                // Clear the product view as it is no longer needed
                productView = null;
                System.gc();

                // Allocate int buffer for pixel conversion and copy pixels
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
                bitmap.getPixels(data, 0, bitmap.getWidth(), 0, 0,
                        bitmap.getWidth(), bitmap.getHeight());

                // Recycle the bitmap object as it is no longer needed
                bitmap.recycle();
                bitmap = null;
                c = null;
                System.gc();

                // Generates the Texture from the int buffer
                mBookDataTexture = Texture.loadTextureFromIntBuffer(data,
                        width, height);

                // Clear the int buffer as it is no longer needed
                data = null;
                System.gc();

                // Hides the loading dialog from a UI thread
                loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

                mIsLoadingBookData = false;

                productTextureIsCreated();
            }
        }
    }

    public void reDrawTextures(){
        // Generates a View to display the book data

        BookOverlayView productView = new BookOverlayView(
                Books.this);

        // Sets the layout params
        productView.setLayoutParams(new LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // Sets View measure - This size should be the same as the
        // texture generated to display the overlay in order for the
        // texture to be centered in screen
        productView.measure(MeasureSpec.makeMeasureSpec(mTextureSize,
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                mTextureSize, MeasureSpec.EXACTLY));

        productView.setAll(hashItemToStateToList,0,1);

        // updates layout size
        productView.layout(0, 0, productView.getMeasuredWidth(),
                productView.getMeasuredHeight());

        // Draws the View into a Bitmap. Note we are allocating several
        // large memory buffers thus attempt to clear them as soon as
        // they are no longer required:
        Bitmap bitmap = Bitmap.createBitmap(mTextureSize, mTextureSize,
                Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        productView.draw(c);

        // Clear the product view as it is no longer needed
        productView = null;
        System.gc();

        // Allocate int buffer for pixel conversion and copy pixels
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(data, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        // Recycle the bitmap object as it is no longer needed
        bitmap.recycle();
        bitmap = null;
        c = null;
        System.gc();

        // Generates the Texture from the int buffer
        mBookDataTexture = Texture.loadTextureFromIntBuffer(data,
                width, height);

        // Clear the int buffer as it is no longer needed
        data = null;
        System.gc();

        // Hides the loading dialog from a UI thread
        loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

        mIsLoadingBookData = false;

        productTextureIsCreated();

    }

    /**
     * Downloads and image from an Url specified as a paremeter returns the
     * array of bytes with the image Data for storing it on the Local Database
     */
    private byte[] downloadImage(final String imageUrl)
    {
        ByteArrayBuffer baf = null;

        try
        {
            URL url = new URL(imageUrl);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 128);
            baf = new ByteArrayBuffer(128);

            // get the bytes one by one
            int current = 0;
            while ((current = bis.read()) != -1)
            {
                baf.append((byte) current);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (baf == null)
        {
            return null;
        } else
        {
            return baf.toByteArray();
        }
    }


    /** Returns the current Book Data Texture */
    public Texture getProductTexture()
    {
        return mBookDataTexture;
    }


    /** Updates a BookOverlayView with the Book data specified in parameters */
    private void updateProductView(BookOverlayView productView, Book book, int object, int side)
    {

        productView.setAll(hashItemToStateToList,count,0);
        count = 0;
        //productView.setBookTitle(book.getTitle());
        //book.setTitle("martsad");
        //productView.setBookPrice(book.getPriceList());
        //productView.setYourPrice(book.getPriceYour());
        //productView.setBookRatingCount(book.getRatingTotal());
        //productView.setRating(book.getRatingAvg());
        //productView.setBookAuthor(book.getAuthor());
        productView.setCoverViewFromBitmap(book.getThumb());
        //productView.setRightMoveArrow(0);


    }


    /**
     * Starts application content Mode Displays UI OVerlays and turns Cloud
     * Recognition off
     */
    public void enterContentMode()
    {
        // Updates state variables
        mBookInfoStatus = BOOKINFO_IS_DISPLAYED;

        // Shows the 2D Overlay
        show2DOverlay();

        // Enters content mode to disable Cloud Recognition
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        TargetFinder targetFinder = objectTracker.getTargetFinder();

        // Stop Cloud Recognition
        targetFinder.stop();
        scanlineStop();

        // Remember we are in content mode:
        mRenderer.setScanningMode(false);
    }


    /** Hides the 2D Overlay view and starts C service again */
    private void enterScanningMode()
    {
        // Hides the 2D Overlay
        hide2DOverlay();

        // Enables Cloud Recognition Scanning Mode
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        TargetFinder targetFinder = objectTracker.getTargetFinder();

        // Start Cloud Recognition
        targetFinder.startRecognition();

        // Clear all trackables created previously:
        targetFinder.clearTrackables();

        mRenderer.setScanningMode(true);
        scanlineStart();

        // Updates state variables
        mRenderer.showAnimation3Dto2D(false);
        mRenderer.isShowing2DOverlay(false);
        mRenderer.setRenderState(BooksRenderer.RS_SCANNING);
    }


    /** Displays the 2D Book Overlay */
    public void show2DOverlay()
    {
        // Sends the Message to the Handler in the UI thread
        overlay2DHandler.sendEmptyMessage(SHOW_2D_OVERLAY);
    }


    /** Hides the 2D Book Overlay */
    public void hide2DOverlay()
    {
        // Sends the Message to the Handler in the UI thread
        overlay2DHandler.sendEmptyMessage(HIDE_2D_OVERLAY);
    }


    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        return mGestureDetector.onTouchEvent(event);
    }

    // Process Double Tap event for showing the Camera options menu
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        public boolean onSingleTapUp(MotionEvent event)
        {

            // If the book info is not displayed it performs an Autofocus
            if (mBookInfoStatus == BOOKINFO_NOT_DISPLAYED)
            {
                // Calls the Autofocus Method
                boolean result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                if (!result)
                    Log.e("SingleTapUp", "Unable to trigger focus");

                // If the book info is displayed it shows the book data web view
            } else if (mBookInfoStatus == BOOKINFO_IS_DISPLAYED)
            {

                float x = event.getX(0);
                float y = event.getY(0);

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                // Creates a Bounding box for detecting touches
                float screenLeft = metrics.widthPixels / 8.0f;
                float screenRight = metrics.widthPixels * 0.8f;
                float screenUp = metrics.heightPixels / 7.0f;
                float screenDown = metrics.heightPixels * 0.7f;



                Log.d("MARTIN",screenLeft + " " + screenRight + " " + screenUp + " " + screenDown);

                // Checks touch inside the bounding box
                if (x < (screenRight) && x > screenLeft && y < screenDown
                        && y > screenUp)
                {
                    // Starts the webView
                    //Toast.makeText(getApplicationContext(),"Touch URl",Toast.LENGTH_SHORT).show();
                    startWebView(0);
                    scanlineStart();
                }

            }

            return true;
        }
    }


    @Override
    public boolean doLoadTrackersData()
    {
        Log.d(LOGTAG, "initBooks");

        // Get the object tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        // Initialize target finder:
        TargetFinder targetFinder = objectTracker.getTargetFinder();

        // Start initialization:
        if (targetFinder.startInit(kAccessKey, kSecretKey))
        {
            targetFinder.waitUntilInitFinished();
            scanlineStart();
        }

        int resultCode = targetFinder.getInitState();
        if (resultCode != TargetFinder.INIT_SUCCESS)
        {
            if(resultCode == TargetFinder.INIT_ERROR_NO_NETWORK_CONNECTION)
            {
                mInitErrorCode = UPDATE_ERROR_NO_NETWORK_CONNECTION;
            }
            else
            {
                mInitErrorCode = UPDATE_ERROR_SERVICE_NOT_AVAILABLE;
            }

            Log.e(LOGTAG, "Failed to initialize target finder.");
            return false;
        }

        return true;
    }


    @Override
    public boolean doUnloadTrackersData()
    {
        return true;
    }


    @Override
    public void onInitARDone(SampleApplicationException exception)
    {

        if (exception == null)
        {
            initApplicationAR();

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

            // Start the camera:
            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }

            mRenderer.mIsActive = true;

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result)
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

            mUILayout.bringToFront();

            // Hides the Loading Dialog
            loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

            mUILayout.setBackgroundColor(Color.TRANSPARENT);

        } else
        {
            Log.e(LOGTAG, exception.getString());
            if(mInitErrorCode != 0)
            {
                showErrorMessage(mInitErrorCode,10, true);
            }
            else
            {
                showInitializationErrorMessage(exception.getString());
            }
        }
    }


    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        Books.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state)
    {
        // Get the tracker manager:
        TrackerManager trackerManager = TrackerManager.getInstance();

        // Get the object tracker:
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        // Get the target finder:
        TargetFinder finder = objectTracker.getTargetFinder();

        // Check if there are new results available:
        final int statusCode = finder.updateSearchResults();

        // Show a message if we encountered an error:
        if (statusCode < 0)
        {

            boolean closeAppAfterError = (
                    statusCode == UPDATE_ERROR_NO_NETWORK_CONNECTION ||
                            statusCode == UPDATE_ERROR_SERVICE_NOT_AVAILABLE);

            showErrorMessage(statusCode, state.getFrame().getTimeStamp(), closeAppAfterError);

        } else if (statusCode == TargetFinder.UPDATE_RESULTS_AVAILABLE)
        {
            // Process new search results
            if (finder.getResultCount() > 0)
            {
                TargetSearchResult result = finder.getResult(0);
                String metaData = result.getMetaData();
                //Toast.makeText(getApplicationContext(),metaData.get(0),Toast.LENGTH_SHORT).show();
                if (metaData.indexOf('2') >= 0){
                    Toast.makeText(getApplicationContext(),"found coke",Toast.LENGTH_SHORT ).show();
                    count = 1;
                }
                // Check if this target is suitable for tracking:
                if (result.getTrackingRating() > 0)
                {
                    // Create a new Trackable from the result:
                    Trackable newTrackable = finder.enableTracking(result);
                    if (newTrackable != null)
                    {
                        Log.d(LOGTAG, "Successfully created new trackable '"
                                + newTrackable.getName() + "' with rating '"
                                + result.getTrackingRating() + "'.");

                        // Checks if the targets has changed
                        Log.d(LOGTAG, "Comparing Strings. currentTargetId: "
                                + result.getUniqueTargetId() + "  lastTargetId: "
                                + lastTargetId);

                        if (true)
                        {
                            // If the target has changed then regenerate the
                            // texture
                            // Cleaning this value indicates that the product
                            // Texture needs to be generated
                            // again in Java with the new Book data for the new
                            // target
                            mRenderer.deleteCurrentProductTexture();

                            // Starts the loading state for the product
                            mRenderer
                                    .setRenderState(BooksRenderer.RS_LOADING);

                            // Calls the Java method with the current product
                            // texture
                            createProductTexture(result.getMetaData());

                        } else
                            mRenderer
                                    .setRenderState(BooksRenderer.RS_NORMAL);

                        // Initialize the frames to skip variable, used for
                        // waiting
                        // a few frames for getting the chance to tracking
                        // before
                        // starting the transition to 2D when there is no target
                        mRenderer.setFramesToSkipBeforeRenderingTransition(10);

                        // Initialize state variables
                        mRenderer.showAnimation3Dto2D(true);
                        mRenderer.resetTrackingStarted();

                        // Updates the value of the current Target Id with the
                        // new target found
                        synchronized (lastTargetId)
                        {
                            lastTargetId = result.getUniqueTargetId();
                        }

                        enterContentMode();
                    } else
                        Log.e(LOGTAG, "Failed to create new trackable.");
                }
            }
        }
    }


    @Override
    public boolean doInitTrackers()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Indicate if the trackers were initialized correctly
        boolean result = true;

        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }

        return result;
    }


    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;

        // Start the tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        objectTracker.start();

        // Start cloud based recognition if we are in scanning mode:
        if (mRenderer.getScanningMode())
        {
            TargetFinder targetFinder = objectTracker.getTargetFinder();
            targetFinder.startRecognition();
        }

        return result;
    }


    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if(objectTracker != null)
        {
            objectTracker.stop();

            // Stop cloud based recognition:
            TargetFinder targetFinder = objectTracker.getTargetFinder();
            targetFinder.stop();

            // Clears the trackables
            targetFinder.clearTrackables();
        }
        else
        {
            result = false;
        }

        return result;
    }


    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    private void scanlineStart() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanLine.setVisibility(View.VISIBLE);
                scanLine.setAnimation(scanAnimation);
                scanLineStarted = true;
            }
        });
    }

    private void scanlineStop() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanLine.setVisibility(View.GONE);
                scanLine.clearAnimation();
                scanLineStarted = false;
            }
        });
    }

    private void scanCreateAnimation() {
        final int orientation = getResources().getConfiguration().orientation;

        Display display = getWindowManager().getDefaultDisplay();
        int screenHeight = display.getHeight();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            scanAnimation = new TranslateAnimation(
                    TranslateAnimation.ABSOLUTE, 0f,
                    TranslateAnimation.ABSOLUTE, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                    TranslateAnimation.ABSOLUTE, screenHeight);
            scanAnimation.setDuration(4000);
        } else {
            scanAnimation = new TranslateAnimation(
                    TranslateAnimation.ABSOLUTE, 0f,
                    TranslateAnimation.ABSOLUTE, 0f,
                    TranslateAnimation.ABSOLUTE, screenHeight,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f);
            scanAnimation.setDuration(2000);
        }

        scanAnimation.setRepeatCount(-1);
        scanAnimation.setRepeatMode(Animation.REVERSE);
        scanAnimation.setInterpolator(new LinearInterpolator());

        // if the animation was in progress we need to restart it
        // to take into account the new configuration
        if (scanLineStarted) {
            scanlineStop();
            scanlineStart();
        }
    }
}