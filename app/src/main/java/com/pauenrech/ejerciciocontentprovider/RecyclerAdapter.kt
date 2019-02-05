package com.pauenrech.ejerciciocontentprovider

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.product_card.view.*

class RecyclerAdapter(private val context: Context,
                      private var lista: MutableList<Producto>,
                      private val density: Float,
                      private val longClickListener: onInteraction): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    interface onInteraction{
        fun sendLongClickEvent(producto: Producto, posicion: Int)
        fun sendClickEvent(producto: Producto, posicion: Int, view: View)
    }

    private var isSelectingElements = false

    fun changeMode(mode: Boolean){
        isSelectingElements = mode
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val celda = LayoutInflater.from(context).inflate(R.layout.product_card, p0, false)
        return ViewHolder(celda)
    }

    override fun getItemCount(): Int {
       return lista.size
    }


   override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.producto = lista[position]
       holder.nombre.text = lista[position].nombre
       holder.cantidad.text = lista[position].cantidad.toString()
       holder.posicion = position

       holder.cantidad.transitionName = MainActivity.TRANSITION_PRODUCT_QUANTITY + position
       holder.nombre.transitionName = MainActivity.TRANSITION_PRODUCT_NAME + position
       holder.card.transitionName = MainActivity.TRANSITION_PRODUCT_CARD + position

    }

    /*
    *
    *  Si estoy seleccionando elementos y se recicla un view, no lo cambio para que no pierdan la seleccion los
    *  ya seleccionados que puedan ser reciclados. Por otro lado, si no estoy seleccionando, siempre que reciclo
    *  vuelvo a las view al estado inicial. Esto es necesario porque al borrar se elimina una view con el seleccionado
    *  Aplicado y al reciclarse aparecería el nuevo elemento seleccionado sin estarlo. He probado a deseleccionar todos
    *  los elementos antes de borrar nada (Como hago al salir del modo seleccion) pero en este caso no funciona, asi
    *  que la solución que he encontrado es esta.
    *
    *
    * */
    override fun onViewRecycled(holder: ViewHolder) {
        if (!isSelectingElements){
            holder.card.elevation = 2 * density
            holder.card.setCardBackgroundColor(context.getColor(android.R.color.background_light))
            holder.nombre.setTextColor(context.getColor(R.color.colorText))
            holder.cantidad.setTextColor(context.getColor(R.color.colorText))
        }
        super.onViewRecycled(holder)
    }

    //Utilizó un payload junto con notifyItemChanged para poder relacionar el cambio que debe realizar la UI de una card producto cuando es seleccionada
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.producto = lista[position]
        holder.nombre.text = lista[position].nombre
        holder.cantidad.text = lista[position].cantidad.toString()
        holder.posicion = position

        holder.cantidad.transitionName = MainActivity.TRANSITION_PRODUCT_QUANTITY + position
        holder.nombre.transitionName = MainActivity.TRANSITION_PRODUCT_NAME + position
        holder.card.transitionName = MainActivity.TRANSITION_PRODUCT_CARD + position

        if (payloads.isNotEmpty()){
           holder.setSelectedState(payloads[0] as Boolean)
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var producto : Producto? = null
        var posicion: Int? = null
        val card = itemView.cardProductCard
        val nombre = itemView.cardProductName
        val cantidad = itemView.cardProductQuantity

        fun setSelectedState(select: Boolean){
            when(select){
                true -> {
                    card.setCardBackgroundColor(context.getColor(android.R.color.darker_gray))
                    nombre.setTextColor(context.getColor(android.R.color.background_light))
                    cantidad.setTextColor(context.getColor(android.R.color.background_light))
                    card.elevation = (6 * density)
                }
                else -> {
                    card.elevation = 2 * density
                    card.setCardBackgroundColor(context.getColor(android.R.color.background_light))
                    nombre.setTextColor(context.getColor(R.color.colorText))
                    cantidad.setTextColor(context.getColor(R.color.colorText))
                }
            }
        }


        init {

            card.setOnClickListener {
                longClickListener.sendClickEvent(producto!!,posicion!!,itemView)
            }

            card.setOnLongClickListener {
                    longClickListener.sendLongClickEvent(producto!!, posicion!!)
                true
            }
        }
    }

}