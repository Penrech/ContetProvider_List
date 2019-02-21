package com.pauenrech.ejerciciocontentprovider


import android.app.Activity
import android.content.ContentProviderClient
import android.content.ContentValues
import android.content.Intent
import android.graphics.ColorFilter
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.airbnb.lottie.*
import com.airbnb.lottie.model.KeyPath
import kotlin.math.roundToInt
import com.airbnb.lottie.value.LottieValueCallback
import com.airbnb.lottie.LottieProperty
import com.pauenrech.ejerciciocontentprovider.provider.MyContentProvider
import kotlinx.android.synthetic.main.add_product_form.*
import kotlinx.android.synthetic.main.product_card.view.*
import kotlinx.android.synthetic.main.selected_counter.view.*

class MainActivity : AppCompatActivity(),
    RecyclerAdapter.onInteraction{

    companion object {
        val TABLE_PRODUCTS = "products"
        val COLUMN_ID = "_id"
        val COLUMN_PRODUCTNAME = "productname"
        val COLUMN_QUANTITY = "quantity"
        val TRANSITION_PRODUCT_NAME = "productName"
        val TRANSITION_PRODUCT_QUANTITY = "productQuantity"
        val TRANSITION_PRODUCT_CARD = "productCard"
        val PRODUCT_ID = "id"
        val PRODUCT_NAME = "productName"
        val PRODUCT_QUANTITY = "productQuantity"
        val PRODUCT_POSITION = "productPosition"
        val EDIT_REQUEST_CODE = 101
    }

    private var animatedAddIcon: LottieDrawable? = null
    private var animatedOrderIcon: LottieDrawable? = null
    private var menuObject: Menu? = null

    private var needToAddIconToOrder: Boolean = true
    private var addFormEnabled: Boolean = false

    private var keyboardInput : InputMethodManager? = null

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerAdapter? = null

    private var productosSeleccionados: HashMap<Int,Int>? = null

    private var density: Float? = null

    private var addOpen: Boolean = false
    private var orderByNameDesc: Boolean = true

    private var outTransition: Transition? = null

    private var LEFT_CARD_MARGINS: Int? = null
    private var BOTTOM_CARD_MARGIN: Int? = null
    private var CARD_SIZE: Int? = null
    private var CARD_HIGH_OPENED: Int? = null
    private var CARD_RADIUS_OPENED: Int? = null
    private var CARD_RADIUS_CLOSED: Int? = null

    private var openCloseTransition: Transition? = null
    private var showHideTranstion: Transition? = null
    private var showHideActionsTransition: Transition? = null

    private var lottieFailed: Boolean = false
    private var selectionModeOn: Boolean = false

    private var uri: Uri? = null
    private var contentProviderClient: ContentProviderClient? = null

   val lista = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        density = resources.displayMetrics.density
        keyboardInput = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        LEFT_CARD_MARGINS = (16 * density!!).roundToInt()
        BOTTOM_CARD_MARGIN = (24 * density!!).roundToInt()
        CARD_SIZE = (64 * density!!).roundToInt()
        CARD_HIGH_OPENED = (88 * density!!).roundToInt()
        CARD_RADIUS_OPENED = (2 * density!!).roundToInt()
        CARD_RADIUS_CLOSED = (32 * density!!).roundToInt()

        openCloseTransition = TransitionInflater.from(this).inflateTransition(R.transition.open_close_add)
        showHideTranstion = TransitionInflater.from(this).inflateTransition(R.transition.hide_popup)
        outTransition = TransitionInflater.from(this).inflateTransition(R.transition.hide_popup)
        showHideActionsTransition = TransitionInflater.from(this).inflateTransition(R.transition.show_hide_actions)
        val transitionEnter = TransitionInflater.from(this).inflateTransition(R.transition.enter_shared_transition)
        window.sharedElementEnterTransition = transitionEnter

        productosSeleccionados = hashMapOf()

        layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapter(this, lista, density!!,this)
        productsRV.adapter = adapter
        productsRV.layoutManager = layoutManager

        configurarContentProvider()
    }

    //Me aseguro que lo primero que se haga es configurar el contentProvider
    fun configurarContentProvider(){
        //uri = Uri.parse("content://com.cice.formador.androidstorage.MyContentProvider/products")
        uri = MyContentProvider.CONTENT_URI
        contentProviderClient = contentResolver.acquireContentProviderClient(uri)

        getList()
        setLottie()
        implementarAddButton()
        implementSearchBox()
        implementCloseButton()
        implementCloseSelectionButton()
    }

    //------------Métodos contentProvider ----------
    /*
    *
    * No he añadido estos métodos en una clase aparte porque tiene muchas corelaciones con el main (lista, adapter,
    * productosSeleccionados...) y para solucionarlo tenia que añadir bastantes interfaces y/o más variables todavía
    * al companion object. Aun asi, se que en este caso si sería más recomendable separar
    *
    * */

    fun getList(){
        lista.clear()

        var order = "$COLUMN_PRODUCTNAME ASC"
        if (!orderByNameDesc) order = "$COLUMN_PRODUCTNAME DESC"

        val projection = arrayOf(COLUMN_ID, COLUMN_PRODUCTNAME, COLUMN_QUANTITY)
        val cursor = contentProviderClient!!.query(uri,
            projection,
            null,
            null, order)

        if (!cursor.moveToFirst()){

        } else{
            do{
                val id = Integer.parseInt(cursor.getString(0))
                val nombre = cursor.getString(1)
                val cantidad = Integer.parseInt(cursor.getString(2))
                val producto = Producto(id, nombre, cantidad)
                lista.add(producto)
            } while (cursor.moveToNext())

            cursor.close()
        }

        adapter!!.notifyDataSetChanged()
    }

    fun addProducto(nombre: String, cantidad: String){

        val producto = ContentValues()
        producto.put(COLUMN_PRODUCTNAME, nombre)
        producto.put(COLUMN_QUANTITY, cantidad)

        val respuesta = contentProviderClient!!.insert(uri, producto)

        if (respuesta != null){
            getList()
        } else {
            showSnackbar("Error añadiendo producto")
        }
    }

    fun findProducto(busqueda: String){
        lista.clear()

        var order = "$COLUMN_PRODUCTNAME ASC"
        if (!orderByNameDesc) order = "$COLUMN_PRODUCTNAME DESC"

        val projection = arrayOf(COLUMN_ID, COLUMN_PRODUCTNAME, COLUMN_QUANTITY)
        val selection = "$COLUMN_PRODUCTNAME LIKE '%$busqueda%'"
        val cursor = contentProviderClient!!.query(uri,
            projection,
            selection,
            null, order)

        if (!cursor.moveToFirst()){

        } else{
            do{
                val id = Integer.parseInt(cursor.getString(0))
                val nombre = cursor.getString(1)
                val cantidad = Integer.parseInt(cursor.getString(2))
                val producto = Producto(id, nombre, cantidad)
                lista.add(producto)
            } while (cursor.moveToNext())

            cursor.close()
        }
        adapter!!.notifyDataSetChanged()
    }

    fun updateProducto(nuevoNombre: String, nuevaCantidad: Int, id: Int, posicion: Int){
        val nombreActual = lista[posicion].nombre
        val cantidadActual = lista[posicion].cantidad

        if (nuevoNombre == nombreActual && nuevaCantidad == cantidadActual) return
        val producto = ContentValues()

        if (nuevoNombre != nombreActual){
            producto.put(COLUMN_PRODUCTNAME, nuevoNombre)
        }
        if (nuevaCantidad != cantidadActual){
            producto.put(COLUMN_QUANTITY, nuevaCantidad)
        }

        val selection = "$COLUMN_ID = $id"
        val respuesta = contentProviderClient!!.update(uri,
            producto,
            selection,
            null)

       if (respuesta > 0){
           getList()
       } else {
           showSnackbar("Error actualizando producto")
       }


    }

    fun deleteProducts(){
        val listaIdsABorrar = mutableListOf<Int>()
        val listaPosiciones = mutableListOf<Int>()
        productosSeleccionados!!.forEach {
            listaIdsABorrar.add(it.key)
            listaPosiciones.add(it.value)
        }

        val productosABorrar = listaIdsABorrar.joinToString(prefix = "(", postfix  = ")")
        val selection = "$COLUMN_ID in $productosABorrar"
        val resultado = contentProviderClient!!.delete(uri,
            selection,
            null)

        if (resultado > 0){
            productosSeleccionados!!.clear()
            salirModoSeleccion()
            listaIdsABorrar.forEach {id ->
                val element = lista.filter { it.id == id}
                val index = lista.indexOf(element[0])
                lista.removeAt(index)
                adapter!!.notifyItemRemoved(index)
                adapter!!.notifyItemRangeChanged(index,lista.size)
            }
        } else {
            showSnackbar("Error borrando productos")
        }
    }

    //----------Fin metodos contentProvider----------------


    //Configuración de Lottie para las animaciones
    /*
    *
    * Lottie puede fallar y no cargar bien la animación(durante to_do el desarrollo no me ha pasado ni una vez, pero es
    * posible) Asi que también he implementado la variante de iconos por si lottie fallara
    *
    * */
    fun setLottie(){
        val task: LottieTask<LottieComposition> = LottieCompositionFactory.fromRawRes(this,R.raw.animation_48b)

        task.addListener { result ->
            animatedAddIcon = LottieDrawable()
            animatedAddIcon!!.composition = result
            animatedAddIcon!!.enableMergePathsForKitKatAndAbove(true)
            animatedAddIcon!!.scale = 0.75f

            val colorInicial = getColor(android.R.color.background_light)
            val colorFinal = getColor(R.color.colorText)
            animatedAddIcon!!.addValueCallback(
                KeyPath("**"),
                LottieProperty.COLOR)
            { frameInfo -> if (frameInfo.overallProgress < 0.5) colorInicial else colorFinal }

            imageButton.setImageDrawable(animatedAddIcon)
        }

        task.addFailureListener {
            lottieFailed = true
        }

        val task2: LottieTask<LottieComposition> = LottieCompositionFactory.fromRawRes(this,R.raw.animation_order_48b)

        task2.addListener { result ->
            animatedOrderIcon = LottieDrawable()
            animatedOrderIcon!!.composition = result
            animatedOrderIcon!!.enableMergePathsForKitKatAndAbove(true)
            animatedOrderIcon!!.setMaxFrame(5)
            animatedOrderIcon!!.scale = 0.75f

            val filter = SimpleColorFilter(getColor(R.color.colorText))
            val keyPath = KeyPath("**")
            val callback = LottieValueCallback<ColorFilter>(filter)
            animatedOrderIcon!!.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)


            if (needToAddIconToOrder){
                menuObject?.let {
                    it.getItem(0).icon = animatedOrderIcon
                    needToAddIconToOrder = !needToAddIconToOrder
                }
            }
        }

        task2.addFailureListener {
            lottieFailed = true
        }
    }

    //Método implementación del botón flotante de añadir productos
    fun implementarAddButton(){
        imageButton.setOnClickListener {vista ->
            if (!addOpen){
                openAddElementUI()
            } else {
                closeAddElementUI()
            }
        }
    }

    //Método implementación del botón de cerrar campo de busqueda que aparece al darle focus al campo de busqueda
    fun implementCloseButton(){
        btnCerrar.setOnClickListener {
            showCloseButton(false)
        }
    }

    //Método implementación del botón de cerrar el modo selección
    fun implementCloseSelectionButton(){
        btnCerrarSeleccion.setOnClickListener {
            salirModoSeleccion()
        }
    }

    //Método implementación del campo de texto buscar
    fun implementSearchBox(){
        searchBox.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                showCloseButton(true)
            }
        }
        searchBox.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null){
                    findProducto(s.toString())
                }
            }
        })
    }

    //Método para mostrar o ocultar el botón de cerrar campo de texto, es true cuando el campo de texto recibe focus
    fun showCloseButton(show: Boolean){
        TransitionManager.beginDelayedTransition(root,showHideTranstion)
        if (show){
            btnCerrar.visibility = View.VISIBLE
        }
        else{
            btnCerrar.visibility = View.GONE
            searchBox.text!!.clear()
            closeKeyboard()
        }
    }

    //Método para cerrar manualmente el teclado ya que en algunos momentos se podía quedar abierto al cambiar entre algunas vistas
    fun closeKeyboard(){
        val vistaActiva = this.currentFocus

        vistaActiva?.let {
            keyboardInput?.hideSoftInputFromWindow(it.windowToken, 0);
            it.clearFocus()
        }
    }

    //-----------Métodos card flotante añadir producto----------------
    // Método para abrir la card flotante, se realizan muchos ajustes de UI de forma dinámica a través de código
    fun openAddElementUI(){
        TransitionManager.beginDelayedTransition(root,openCloseTransition)
        val set = ConstraintSet()
        set.clone(rootFloating)
        set.connect(floatingCard.id,ConstraintSet.START,rootFloating.id,ConstraintSet.START,0)
        set.connect(floatingCard.id,ConstraintSet.END,rootFloating.id,ConstraintSet.END,0)
        set.connect(floatingCard.id,ConstraintSet.BOTTOM,rootFloating.id,ConstraintSet.BOTTOM,0)
        set.constrainWidth(floatingCard.id,ConstraintSet.MATCH_CONSTRAINT)
        set.applyTo(rootFloating)

        floatingCard.setCardBackgroundColor(getColor(android.R.color.background_light))
        floatingCard.radius = CARD_RADIUS_OPENED!!.toFloat()
        floatingCard.layoutParams.height = CARD_HIGH_OPENED!!

        val params = imageButton.layoutParams as ConstraintLayout.LayoutParams
        params.horizontalBias = 1f
        imageButton.layoutParams = params

        includeNewProductForm.visibility = View.VISIBLE
        addFormEnabled = true

        if (lottieFailed){
            imageButton.setImageResource(R.drawable.ic_round_done_36)
            imageButton.setColorFilter(getColor(R.color.colorText))
        }

        addOpen = !addOpen
        animatedAddIcon?.let {
            it.frame = 1
            it.speed = 1f
            it.playAnimation()
        }

        showCloseButton(false)
        salirModoSeleccion()

    }

    //Método para cerrar la card flotante de añadir producto
    fun closeAddElementUI(){
        TransitionManager.beginDelayedTransition(root,openCloseTransition)
        val set = ConstraintSet()
        set.clone(rootFloating)
        set.connect(floatingCard.id,ConstraintSet.START,rootFloating.id,ConstraintSet.START,LEFT_CARD_MARGINS!!)
        set.connect(floatingCard.id,ConstraintSet.END,rootFloating.id,ConstraintSet.END,LEFT_CARD_MARGINS!!)
        set.connect(floatingCard.id,ConstraintSet.BOTTOM,rootFloating.id,ConstraintSet.BOTTOM,BOTTOM_CARD_MARGIN!!)
        set.applyTo(rootFloating)

        floatingCard.setCardBackgroundColor(getColor(android.R.color.darker_gray))
        floatingCard.radius = CARD_RADIUS_CLOSED!!.toFloat()
        floatingCard.layoutParams.height = CARD_SIZE!!
        floatingCard.layoutParams.width = CARD_SIZE!!

        val params = imageButton.layoutParams as ConstraintLayout.LayoutParams
        params.horizontalBias = 0.5f
        imageButton.layoutParams = params

        closeKeyboard()
        includeNewProductForm.visibility = View.GONE
        addFormEnabled = false

        if (lottieFailed){
            imageButton.setImageResource(R.drawable.ic_round_add_36)
            imageButton.setColorFilter(getColor(android.R.color.background_light))
        }

       addOpen = !addOpen
        animatedAddIcon?.let {
            it.frame = 5
            it.speed = -1f
            it.playAnimation()
        }

        addElementToLista()
    }

    //Método al que se llama cada vez que se cierra la card flotante de añadir producto para añadir el producto insertado
    fun addElementToLista(){
        if (inputNombre.text.isNullOrEmpty()){
            showSnackbar("Producto sin nombre no añadido")
        }
        else if (inputCantidad.text.isNullOrEmpty()){
            showSnackbar("Producto sin cantidad no añadido")
        }
        else{
            addProducto(inputNombre.text.toString(),inputCantidad.text.toString())
        }
        inputNombre.text!!.clear()
        inputCantidad.text!!.clear()
    }

    //Método para mostar un snackbar sin tener que repetir su código, se usa mucho ya que no uso Toast, solo Snackbars
    fun showSnackbar(message: String){
        val snackbar = Snackbar.make(rootContent,message,Snackbar.LENGTH_SHORT)
        snackbar.show()
    }

    //Método para seleccionar un producto si el modo selección esta activo
    fun seleccionarProducto(id:Int, posicion: Int){
        productosSeleccionados!!.put(id,posicion)
        adapter!!.notifyItemChanged(posicion,true)
        if (productosSeleccionados!!.size == 1){
            entrarModoSeleccion()
        }
        cambiarMenuCounter()
    }

    //Método para desseleccionar un producto si el modo selección esta activo
    fun deseleccionarProducto(id: Int, posicion: Int){
        productosSeleccionados!!.remove(id)
        adapter!!.notifyItemChanged(posicion,false)
        if (productosSeleccionados!!.size < 1){
            salirModoSeleccion()
        } else{
            cambiarMenuCounter()
        }
    }

    //Método para cambiar el número que indica en el modo selección cuandos productos están seleccionados
    fun cambiarMenuCounter(){
        val counter = menuObject!!.getItem(1).actionView.counterTextView
        counter.text = productosSeleccionados!!.size.toString()
    }

    //Método que se llama al entrar al modo selección
    fun entrarModoSeleccion(){
        menuObject!!.getItem(0).isVisible = false
        menuObject!!.getItem(1).isVisible = true
        menuObject!!.getItem(2).isVisible = true
        showCloseButton(false)
        searchBox.visibility = View.GONE
        btnCerrarSeleccion.visibility = View.VISIBLE

        if (addFormEnabled)
            closeAddElementUI()

        selectionModeOn = true
        adapter!!.changeMode(selectionModeOn)
    }

    //Método que se llama al salir del modo selección
    fun salirModoSeleccion(){
        TransitionManager.beginDelayedTransition(root,showHideActionsTransition)
        menuObject!!.getItem(0).isVisible = true
        menuObject!!.getItem(1).isVisible = false
        menuObject!!.getItem(2).isVisible = false
        searchBox.visibility = View.VISIBLE
        btnCerrarSeleccion.visibility = View.GONE

        deseleccionarTodo()

        selectionModeOn = false
        adapter!!.changeMode(selectionModeOn)

    }

    //Método para deseleccionar todos los elementos seleccionados al salir, se llama principalmente desde el método
    // salirModoSeleccion, en algún caso no me ha funcionado como esperaba, expongo más en la clase del recyclerAdapter
    fun deseleccionarTodo(){
        if (productosSeleccionados!!.isNotEmpty()){
            productosSeleccionados!!.forEach {
                Log.i("TAG","Deselecciono ${it.value}")
                adapter!!.notifyItemChanged(it.value,false)
            }
            productosSeleccionados!!.clear()
        }
    }

    //Método que abre una nueva activity para editar los datos del producto seleccionado
    fun editarElemento(elemento: Int, elementView: View){
        val transitionProductName = TRANSITION_PRODUCT_NAME + elemento
        val transitionProductQuantity = TRANSITION_PRODUCT_QUANTITY + elemento
        val transitionProductCard = TRANSITION_PRODUCT_CARD + elemento

        val pairEditNombre = Pair<View,String>(elementView.cardProductName,transitionProductName)
        val pairEditCantidad = Pair<View,String>(elementView.cardProductQuantity,transitionProductQuantity)
        val pairEditCard = Pair<View,String>(elementView.cardProductCard, transitionProductCard)

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@MainActivity, pairEditNombre, pairEditCantidad, pairEditCard
        )
        val intent = Intent(this,ChangeDataActivity::class.java)
        intent.putExtra(PRODUCT_POSITION,elemento)
        intent.putExtra(PRODUCT_NAME,lista[elemento].nombre)
        intent.putExtra(PRODUCT_ID,lista[elemento].id)
        intent.putExtra(PRODUCT_QUANTITY,lista[elemento].cantidad.toString())

        startActivityForResult(intent,EDIT_REQUEST_CODE,options.toBundle())
    }

    //Método que se llama cada vez que se hace click en un producto, su funcionalidad cambia en función de si se está
    //en modo seleccion o no
    override fun sendClickEvent(producto: Producto, posicion: Int, view: View) {
        if (productosSeleccionados!!.isNotEmpty()){
            if (productosSeleccionados!!.contains(producto.id)){
                deseleccionarProducto(producto.id,posicion)
            }
            else{
                seleccionarProducto(producto.id,posicion)
            }
        } else{
            editarElemento(posicion,view)
        }
    }

    //Método que se llama cada vez que se hace un longClick en un producto, este método es el que inicia el modo selección
    override fun sendLongClickEvent(producto: Producto, posicion: Int) {
        if (productosSeleccionados!!.isNotEmpty()) return

        seleccionarProducto(producto.id,posicion)
    }

    //Cuando creo las opciones del menú también me aseguro de que Lottie no halla fallado para añadir iconos estáticos
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (needToAddIconToOrder){
            animatedOrderIcon?.let {
                menu.getItem(0).icon = it
                needToAddIconToOrder = !needToAddIconToOrder
            }
        }
        menuObject = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //Aqui se cambia el orden de la lista a través del botón del menu destinado a ello
        return when (item.itemId) {
            R.id.action_order -> {
                if (!orderByNameDesc){
                    animatedOrderIcon?.let {
                        it.frame = 1
                        it.speed = -1f
                        it.playAnimation()
                    }
                    if (lottieFailed){
                        item.setIcon(R.drawable.ic_round_text_rotate_up_36)
                    }
                } else {
                    animatedOrderIcon?.let {
                        it.frame = 5
                        it.speed = 1f
                        it.playAnimation()
                    }
                    if (lottieFailed){
                        item.setIcon(R.drawable.ic_round_text_rotation_down_36)
                    }
                }
                orderByNameDesc = !orderByNameDesc
                getList()
                true
            }
            R.id.action_delete -> {
                deleteProducts()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //En este método recibo los datos de la actividad de edición y gestiono si hay errores o no. De no haberlos,
    //Modifico el producto con una de los métodos del contentProvider
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == EDIT_REQUEST_CODE){
                if (data == null){
                    showSnackbar("Error guardando datos")
                } else {
                    val nuevoNombre = data.getStringExtra(PRODUCT_NAME)
                    val nuevaCantidad = data.getIntExtra(PRODUCT_QUANTITY, -1)
                    val identificador = data.getIntExtra(PRODUCT_ID, -1)
                    val posicion = data.getIntExtra(PRODUCT_POSITION,-1)
                    if (nuevoNombre.isNullOrEmpty() || nuevaCantidad == -1 || identificador == -1 || posicion == -1){
                        showSnackbar("Error guardando datos")
                    } else {
                        updateProducto(nuevoNombre,nuevaCantidad,identificador, posicion)
                    }
                }
            }
         }
    }

    override fun onBackPressed() {
        if (addFormEnabled){
            closeAddElementUI()
        } else if (selectionModeOn){
            salirModoSeleccion()
        }
        else {
            super.onBackPressed()
        }

    }

}
