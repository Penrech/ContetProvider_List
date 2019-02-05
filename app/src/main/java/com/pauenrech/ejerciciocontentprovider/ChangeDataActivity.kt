package com.pauenrech.ejerciciocontentprovider

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import kotlinx.android.synthetic.main.dialog_view.*

class ChangeDataActivity : AppCompatActivity() {

    var position: Int? = null
    var id: Int? = null
    var guardarClick: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_view)

        val transitionEnter = TransitionInflater.from(this).inflateTransition(R.transition.enter_shared_transition)
        window.sharedElementEnterTransition = transitionEnter

        position = intent.getIntExtra(MainActivity.PRODUCT_POSITION,-1)
        id = intent.getIntExtra(MainActivity.PRODUCT_ID, -1)
        val productName = intent.getStringExtra(MainActivity.PRODUCT_NAME)
        val productQuantity = intent.getStringExtra(MainActivity.PRODUCT_QUANTITY)

        if (position == -1 || productName.isNullOrEmpty() || productQuantity.isNullOrEmpty() || id == -1) supportFinishAfterTransition()

        changeDataCard.transitionName = MainActivity.TRANSITION_PRODUCT_CARD + position
        textInputLayoutCN.transitionName = MainActivity.TRANSITION_PRODUCT_NAME + position
        textInputLayoutCC.transitionName = MainActivity.TRANSITION_PRODUCT_QUANTITY + position

        changeNombre.setText(productName)
        changeCantidad.setText(productQuantity)

        setDismissActivity()
    }

    //Aunque simula a un dialog, es una activity para poder implementar la animación con shared Transition.
    //Así que para simular que se pueda cancelar al hacer click fuera del card, añado un click listener al elemento padre para conseguir este resultado
    fun setDismissActivity(){
        customDialog.setOnClickListener {
            supportFinishAfterTransition()
        }
    }

    fun guardarCambios(view: View){
        guardarClick = true
        supportFinishAfterTransition()
    }

    override fun supportFinishAfterTransition() {
        if (guardarClick){
            val intent = Intent()
            intent.putExtra(MainActivity.PRODUCT_NAME,changeNombre.text.toString())
            var cantidad = -1
            if (!changeCantidad.text.isNullOrEmpty()){
                cantidad = Integer.parseInt(changeCantidad.text.toString())
            }
            intent.putExtra(MainActivity.PRODUCT_QUANTITY,cantidad)
            intent.putExtra(MainActivity.PRODUCT_ID,id)
            intent.putExtra(MainActivity.PRODUCT_POSITION,position)
            setResult(Activity.RESULT_OK,intent)
        }
        else{
            //Si se cancela (Si no se da click en el icono done) no se evaluan ni guardan los cambios
            setResult(Activity.RESULT_CANCELED)
        }
        super.supportFinishAfterTransition()
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }
}
