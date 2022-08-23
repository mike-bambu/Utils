package com.bambu.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView


class PageHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun openPage(page: Int, pdfReader: PdfReader) {
        pdfReader.openPage(page, itemView.findViewById(R.id.pdf_image))
    }
}