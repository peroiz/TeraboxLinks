package com.teraboxlinks;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.webkit.ValueCallback;
import android.webkit.JsResult;
import android.content.Intent;
import android.net.Uri;
import android.webkit.DownloadListener;
import android.app.DownloadManager;
import android.os.Environment;
import android.content.Context;
import android.graphics.Bitmap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.webkit.JavascriptInterface;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.Gravity;

public class MainActivity extends Activity {
    
    private WebView webView;
    private ProgressBar progressBar;
    private FloatingActionButton chatFab;
    private AlertDialog chatDialog;
    private LinearLayout chatContainer;
    private EditText chatInput;
    private ScrollView chatScrollView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        chatFab = findViewById(R.id.chatFab);
        
        setupWebView();
        setupChat();
        
        // Cargar URL principal
        webView.loadUrl("https://linksterabox.github.io");
    }
    
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        
        // Habilitar JavaScript
        webSettings.setJavaScriptEnabled(true);
        
        // Habilitar DOM Storage
        webSettings.setDomStorageEnabled(true);
        
        // Habilitar base de datos
        webSettings.setDatabaseEnabled(true);
        
        // Habilitar zoom
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        // Soporte para contenido multimedia
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        
        // Modo de carga mixto
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Cache
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        
        // Configurar WebViewClient para manejar navegación
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Abrir links externos en el navegador
                if (url.startsWith("https://linksterabox.github.io")) {
                    // Links del mismo dominio se abren en el WebView
                    view.loadUrl(url);
                    return false;
                } else if (url.startsWith("http://") || url.startsWith("https://")) {
                    // Links externos opcionales - puedes cambiar esto
                    // Para abrirlos en el WebView, cambia a: view.loadUrl(url); return false;
                    view.loadUrl(url);
                    return false;
                } else {
                    // Manejar otros tipos de URLs (tel:, mailto:, etc.)
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, "Error: " + description, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Configurar WebChromeClient para diálogos y progreso
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
            
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Alerta")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> result.confirm())
                    .setCancelable(false)
                    .create()
                    .show();
                return true;
            }
        });
        
        // Manejar descargas
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Descargando archivo...");
                request.setTitle("Descarga");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "terabox_file");
                
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(MainActivity.this, "Descarga iniciada...", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupChat() {
        chatFab.setOnClickListener(v -> showChatDialog());
    }
    
    private void showChatDialog() {
        if (chatDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chat de Soporte");
            
            // Crear layout del chat
            LinearLayout mainLayout = new LinearLayout(this);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(20, 20, 20, 20);
            
            // ScrollView para mensajes
            chatScrollView = new ScrollView(this);
            chatContainer = new LinearLayout(this);
            chatContainer.setOrientation(LinearLayout.VERTICAL);
            chatScrollView.addView(chatContainer);
            
            LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                0
            );
            scrollParams.weight = 1;
            chatScrollView.setLayoutParams(scrollParams);
            
            mainLayout.addView(chatScrollView);
            
            // Input de mensaje
            LinearLayout inputLayout = new LinearLayout(this);
            inputLayout.setOrientation(LinearLayout.HORIZONTAL);
            inputLayout.setPadding(0, 20, 0, 0);
            
            chatInput = new EditText(this);
            chatInput.setHint("Escribe tu mensaje...");
            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            inputParams.weight = 1;
            chatInput.setLayoutParams(inputParams);
            
            inputLayout.addView(chatInput);
            mainLayout.addView(inputLayout);
            
            builder.setView(mainLayout);
            builder.setPositiveButton("Enviar", (dialog, which) -> {
                sendMessage();
            });
            builder.setNegativeButton("Cerrar", null);
            builder.setNeutralButton("Limpiar", (dialog, which) -> {
                chatContainer.removeAllViews();
                showChatDialog();
            });
            
            chatDialog = builder.create();
            
            // Mensaje de bienvenida
            addMessageToChat("¡Hola! Bienvenido al chat de TeraboxLinks. ¿En qué puedo ayudarte?", false);
        }
        
        chatDialog.show();
    }
    
    private void sendMessage() {
        String message = chatInput.getText().toString().trim();
        if (!message.isEmpty()) {
            addMessageToChat(message, true);
            chatInput.setText("");
            
            // Simular respuesta automática
            addMessageToChat("Gracias por tu mensaje. Un operador te responderá pronto.", false);
        }
        showChatDialog();
    }
    
    private void addMessageToChat(String message, boolean isUser) {
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setPadding(20, 10, 20, 10);
        messageView.setTextSize(14);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10);
        
        if (isUser) {
            messageView.setBackgroundColor(0xFF2196F3);
            messageView.setTextColor(0xFFFFFFFF);
            params.gravity = Gravity.END;
        } else {
            messageView.setBackgroundColor(0xFFE0E0E0);
            messageView.setTextColor(0xFF000000);
            params.gravity = Gravity.START;
        }
        
        messageView.setLayoutParams(params);
        chatContainer.addView(messageView);
        
        // Scroll al final
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Manejar botón atrás para navegación en WebView
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
