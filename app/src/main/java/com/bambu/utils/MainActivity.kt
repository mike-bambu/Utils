package com.bambu.utils

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val FILE_NAME = "test.pdf"
        private const val URL = "https://storage.googleapis.com/users-copy-center-od-mx.appspot.com/Converted_PDF5694791231873979903_iOGuXX.pdf?GoogleAccessId=firebase-adminsdk-uuwe7@users-copy-center-od-mx.iam.gserviceaccount.com&Expires=1663784948&Signature=U3th1tXPmio9ZzPvKcHahYKpxLB60Qbw%2Fcfk3kY1I2h1UBMYfsbtGo0F%2BL4JyO9KeH4WEietM2B%2BkyFccxEuSN5MgEFIS3npOEWNoI3j9IX3jO%2FDPhxOs2Yme%2FAzFwvjkmxy7po0Ee2SyASSo8%2F81rrhqAPZJoIw9ZTphll6dV9mz3f1XPsGjS1unsAQKhxSlxk1vqpFkkBuP60E5zAd%2FU6lPgOTSuM1Rtvt3iGsr5t0KP7LDC6urt8Qp165dhC7PmhkZcDYtvniHmOQBS2ZyE5JtyAo2Dxd8wDW2olszurLMs3dv%2Fm3MQF%2F4d0xpNAw6oDlYv4u6ge6%2BTKPWp9mVg%3D%3D"
    }

    private var disposable = Disposables.disposed()
    private var pdfReader: PdfReader? = null

    private val fileDownloader by lazy {
        FileDownload(
            OkHttpClient.Builder().build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RxJavaPlugins.setErrorHandler {
            Log.e("Error", it.localizedMessage)
        }

        pdf_view_pager.adapter = PageAdapter()

        val targetFile = File(cacheDir, FILE_NAME)

        disposable = fileDownloader.download(URL, targetFile)
            .throttleFirst(2, TimeUnit.SECONDS)
            .toFlowable(BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(this, "$it% Descargando", Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(this, "Descarga completa", Toast.LENGTH_SHORT).show()
                pdfReader = PdfReader(targetFile).apply {
                    (pdf_view_pager.adapter as PageAdapter).setupPdfRenderer(this)
                }
            })

        TabLayoutMediator(pdf_page_tab, pdf_view_pager) { tab, position ->
            tab.text = (position + 1).toString()
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        pdfReader?.close()
    }
}