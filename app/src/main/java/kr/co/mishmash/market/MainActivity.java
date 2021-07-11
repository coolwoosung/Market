package kr.co.mishmash.market;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "MainActivity";

    private LocationManager mLocationManager;
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private WebView mWebView;
    private WebSettings mWebSettings;

    private ValueCallback<Uri> filePathCallbackNormal;
    private ValueCallback<Uri[]> filePathCallbackLollipop;
    private final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    private final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2002;
    private final static int QR_REQ_CODE = 2005;

    private WebView mChildWebView;
    private Context mContext;
    private String mMainUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                processMain();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                }
            }
        } else {
            processMain();
        }
    }

    private void processMain() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            mLongitude = lastKnownLocation.getLongitude();
            mLatitude = lastKnownLocation.getLatitude();
            Log.d(TAG, "GPS_PROVIDER : longtitude=" + mLongitude + ", latitude=" + mLatitude);
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        mWebView = (WebView)findViewById(R.id.webViewMain);
        mWebSettings = mWebView.getSettings();

        mWebSettings.setJavaScriptEnabled(true);                                    // 웹페이지 자바스클비트 허용 여부
        //mWebSettings.setSupportMultipleWindows(true);                              // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);               // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true);                                 // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true);                                      // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false);                                         // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false);                                 // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);                       // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setDatabaseEnabled(true);                                      // 로컬저장소 허용 여부
        mWebSettings.setDefaultTextEncodingName("UTF-8");                           //TextEncoding 이름 정의
        mWebSettings.setBlockNetworkImage(false);
        mWebSettings.setLoadsImagesAutomatically(true);
        mWebSettings.setAllowContentAccess(true);
        //mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mWebSettings.setTextZoom(100);
        }
        mWebView.addJavascriptInterface(new WebInterface(mContext, this), "webApp");
        mWebView.setNetworkAvailable(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                AlertDialog.Builder alertB = new AlertDialog.Builder(MainActivity.this);
                alertB.setMessage(message);
                alertB.setTitle("");
                alertB.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertB.show();
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder alertB = new AlertDialog.Builder(MainActivity.this);
                alertB.setMessage(message);
                alertB.setTitle("");

                alertB.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                alertB.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                alertB.show();
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                Log.d(TAG, "3.0 <");
                openFileChooser(uploadMsg, "");
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                Log.d(TAG, "3.0+");
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                i.setType("image/*");

                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.d(TAG, "4.1+");
                openFileChooser(uploadMsg, acceptType);
            }

            // For Android 5.0+
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                Log.d(TAG, "5.0+");

                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }

                filePathCallbackLollipop = filePathCallback;

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                i.setType("image/*");

                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_LOLLIPOP_REQ_CODE);

                return true;
            }

            @Override
            public boolean onCreateWindow(final WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                mChildWebView = new WebView(MainActivity.this);
                WebSettings webSettings = mChildWebView.getSettings();

                webSettings.setJavaScriptEnabled(true);                                    // 웹페이지 자바스클비트 허용 여부
                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);               // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
                webSettings.setLoadWithOverviewMode(true);                                 // 메타태그 허용 여부
                webSettings.setUseWideViewPort(true);                                      // 화면 사이즈 맞추기 허용 여부
                webSettings.setSupportZoom(false);                                         // 화면 줌 허용 여부
                webSettings.setBuiltInZoomControls(false);                                 // 화면 확대 축소 허용 여부
                webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
                webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);                       // 브라우저 캐시 허용 여부
                webSettings.setDomStorageEnabled(true);
                webSettings.setDatabaseEnabled(true);                                      // 로컬저장소 허용 여부
                webSettings.setDefaultTextEncodingName("UTF-8");                           //TextEncoding 이름 정의
                webSettings.setBlockNetworkImage(false);
                webSettings.setLoadsImagesAutomatically(true);
                webSettings.setAllowContentAccess(true);

                mChildWebView.addJavascriptInterface(new WebInterface(mContext, MainActivity.this), "webApp");

                /*
                mChildWebView.setWebChromeClient(new WebChromeClient(){
                    @Override
                    public void onCloseWindow(WebView window) {
                        window.setVisibility(View.GONE);
                        mWebView.removeView(window);
                    }
                });
                */

                mWebView.addView(mChildWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(mChildWebView);
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(mChildWebView);
                dialog.show();

                ((WebView.WebViewTransport)resultMsg.obj).setWebView(mChildWebView);
                resultMsg.sendToTarget();
                return true;
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("intent:")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        intent.addCategory("android.intent.category.BROWSABLE");
                        intent.setComponent(null);
                        intent.setSelector(null);

                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if (existPackage != null) {
                            startActivity(intent);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (url.startsWith("https://play.google.com/store/apps/details?id=") || url.startsWith("market://details?id=")) {
                    Uri uri = Uri.parse(url);
                    String packageName = uri.getQueryParameter("id");
                    if (packageName != null && !packageName.equals("")) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    }
                    return true;
                }

                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mMainUrl = getResources().getString(R.string.main_url);
        mWebView.loadUrl(mMainUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case QR_REQ_CODE :
                if(resultCode == RESULT_OK) {
                    String msg = data.getStringExtra("msg");
                    mWebView.loadUrl("javascript: resultQrCode('" + msg + "');");
                }
                break;
            case FILECHOOSER_NORMAL_REQ_CODE:
                if(filePathCallbackNormal == null)
                    return;
                Uri result = (data == null|| resultCode != RESULT_OK) ? null: data.getData();
                filePathCallbackNormal.onReceiveValue(result);
                filePathCallbackNormal = null;

                break;
            case FILECHOOSER_LOLLIPOP_REQ_CODE:
                if(filePathCallbackLollipop == null)
                    return;

                filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                filePathCallbackLollipop = null;

                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String url = mWebView.getUrl();
            Log.d(TAG, url);
            if(url.equals("http://market.eventlife.co.kr") || url.equals("http://market.eventlife.co.kr/") || url.contains("index.php")) {
                AlertDialog.Builder gsDialog = new AlertDialog.Builder(MainActivity.this);
                gsDialog.setTitle("종료");
                gsDialog.setMessage("마켓을 종료하시겠습니까?");
                gsDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = gsDialog.create();
                dialog.show();

                return false;
            } else {
                if(mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    mWebView.loadUrl(mMainUrl);
                }
                return true;
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            Log.d(TAG + " GPS : ", Double.toString(mLatitude )+ '/' + Double.toString(mLongitude));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            processMain();
        } else {
            Toast.makeText(this, "본 앱을 이용하시려면, 위치정보사용에 동의해 주셔야 합니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}