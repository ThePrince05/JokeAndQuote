import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.jokeandquote.R
import com.project.jokeandquote.model.HistoryRecord
import com.project.jokeandquote.view.RecordDetailDialogFragment
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter :
    ListAdapter<HistoryRecord, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    var onItemLongClick: ((HistoryRecord) -> Unit)? = null



    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clientNameText: TextView = itemView.findViewById(R.id.clientNameText)
        val eventNameText: TextView = itemView.findViewById(R.id.eventNameText)
        val typeText: TextView = itemView.findViewById(R.id.typeText)
        val eventDateText: TextView = itemView.findViewById(R.id.eventDateText)

        init {
            itemView.setOnLongClickListener {
                onItemLongClick?.invoke(getItem(adapterPosition))
                true
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val record = getItem(position)
        holder.clientNameText.text = record.clientName
        holder.eventNameText.text = record.eventName
        holder.typeText.text = record.type

        holder.eventDateText.text = record.eventDate?.let {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                formatter.format(parser.parse(it)!!)
            } catch (e: Exception) {
                it
            }
        } ?: "No Date"

        holder.itemView.setOnClickListener {
            val fragment = RecordDetailDialogFragment.newInstance(record)
            fragment.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, "DetailDialog")
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryRecord>() {
        override fun areItemsTheSame(oldItem: HistoryRecord, newItem: HistoryRecord): Boolean {
            return oldItem.id == newItem.id // use appropriate unique ID field
        }

        override fun areContentsTheSame(oldItem: HistoryRecord, newItem: HistoryRecord): Boolean {
            return oldItem == newItem
        }
    }
}
