import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.cpen321.study_wimme.R

object LoaderDialog {
    private var dialog: AlertDialog? = null

    fun show(context: Context) {
        if (dialog == null) {
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.loader_dialog, null)
            builder.setView(view)
            builder.setCancelable(false) // Prevent navigation
            dialog = builder.create()
        }
        dialog?.show()
    }

    fun hide() {
        dialog?.dismiss()
        dialog = null
    }
}