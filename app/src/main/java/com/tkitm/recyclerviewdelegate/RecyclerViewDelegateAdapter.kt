package com.tkitm.recyclerviewdelegate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class RecyclerViewDelegate {

    abstract val layoutId: Int
    open val itemId: Long? = null

    protected lateinit var actionListener: ActionListener

    open fun ViewHolder.onBindViewHolder(
        context: Context,
        adapterPosition: Int,
        payloads: List<Any>,
    ) {
        onBindViewHolder(context, adapterPosition)
    }

    open fun ViewHolder.onBindViewHolder(context: Context, adapterPosition: Int) {
    }

    open fun ViewHolder.onViewAttachedToWindow(context: Context) {
    }

    open fun ViewHolder.onViewDetachedFromWindow(context: Context) {
    }

    open fun ViewHolder.onUnbindViewHolder(context: Context) {
    }

    fun areItemsTheSame(other: RecyclerViewDelegate): Boolean {
        return layoutId == other.layoutId
    }

    open fun areContentsTheSame(other: RecyclerViewDelegate): Boolean {
        return this === other
    }

    open fun getChangePayload(other: RecyclerViewDelegate): Any? {
        return null
    }

    private fun onBindViewHolder(
        viewHolder: ViewHolder,
        adapterPosition: Int,
        payloads: List<Any>
    ) {
        val context = viewHolder.itemView.context
        viewHolder.onBindViewHolder(context, adapterPosition, payloads)
    }

    private fun onViewAttachedToWindow(viewHolder: ViewHolder) {
        val context = viewHolder.itemView.context
        viewHolder.onViewAttachedToWindow(context)
    }

    private fun onViewDetachedFromWindow(viewHolder: ViewHolder) {
        val context = viewHolder.itemView.context
        viewHolder.onViewDetachedFromWindow(context)
    }

    private fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val context = viewHolder.itemView.context
        viewHolder.onUnbindViewHolder(context)
    }

    open class Adapter(private val actionListener: ActionListener) :
        RecyclerView.Adapter<ViewHolder>() {

        protected val itemList: List<RecyclerViewDelegate>
            get() = internalItemList

        private val internalItemList = ArrayList<RecyclerViewDelegate>()

        fun updateList(newList: List<RecyclerViewDelegate>) {
            if (itemList.isEmpty()) {
                internalItemList.addAll(newList)
                notifyDataSetChanged()
                return
            }

            val diffUtilCallback = DiffUtilCallback(itemList, newList)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            val resultList = diffUtilCallback.getResultList()

            resultList.forEach {
                it.actionListener = actionListener
            }

            internalItemList.clear()
            internalItemList.addAll(resultList)
            diffResult.dispatchUpdatesTo(this)
        }

        final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return ViewHolder(itemView)
        }

        final override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            val recyclerViewDelegate = itemList[position]
            holder.onBind(recyclerViewDelegate)
            recyclerViewDelegate.onBindViewHolder(holder, position, payloads)
        }

        override fun onViewRecycled(holder: ViewHolder) {
            holder.delegate?.onUnbindViewHolder(holder)
            holder.onViewRecycled()
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            holder.delegate?.onViewAttachedToWindow(holder)
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            holder.delegate?.onViewDetachedFromWindow(holder)
        }

        override fun getItemCount(): Int = itemList.size

        override fun getItemViewType(position: Int): Int = itemList[position].layoutId

        override fun getItemId(position: Int): Long {
            return itemList[position].itemId ?: super.getItemId(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var delegate: RecyclerViewDelegate? = null

        fun onBind(recyclerViewDelegate: RecyclerViewDelegate) {
            delegate = recyclerViewDelegate
        }

        fun onViewRecycled() {
            delegate = null
        }
    }

    interface Action

    fun interface ActionListener {
        fun onAction()
    }
}

private class DiffUtilCallback(
    private val oldList: List<RecyclerViewDelegate>,
    private val newList: List<RecyclerViewDelegate>,
) : DiffUtil.Callback() {

    private val resultList = ArrayList<RecyclerViewDelegate>(newList)

    fun getResultList(): List<RecyclerViewDelegate> = resultList

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newList[newItemPosition].areItemsTheSame(oldList[oldItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val areContentsTheSame = newList[newItemPosition].areContentsTheSame(old)

        //Item has no change - will not be replaced
        if (areContentsTheSame) {
            resultList[newItemPosition] = old
        }

        return areContentsTheSame
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return oldList[oldItemPosition].getChangePayload(newList[newItemPosition])
    }
}