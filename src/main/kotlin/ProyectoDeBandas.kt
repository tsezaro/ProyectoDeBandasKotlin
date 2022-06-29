import Vito.Companion.agregarTarea
import java.time.LocalDate

//COMMAND
abstract class Tarea {
    var porcentajeComisionRecoleccion : Double = 0.1
    var fecha: LocalDate = LocalDate.now()
    var cumplida = false
    var personaInvolucrada = Persona()
    var banda = Banda()
    var tareaRealizadaObservers = mutableListOf<TareaRealizadaObserver>() //para el observer

    // Template Method
    open fun realizarTarea(){
        doRealizar()
        cumplida = true
        tareaRealizadaObservers.forEach{ it -> it.TareaRealizada(this)}
        // despues de que se realizo algo que haga algo para el observer
    }

    //Primitiva
    abstract fun doRealizar()

    fun estaPendientePara(unaFecha: LocalDate) = !cumplida &&
            (fecha.month == unaFecha.month && fecha.year == unaFecha.year)

    fun asignarBanda(banda: Banda){
        this.banda = banda
    }

    open fun dineroAGanar() : Double = 0.0

    open fun mensajeClave() = " "

    open fun monto() = 0
}

class recolectarDinero : Tarea(){

    //Primitiva
    override fun doRealizar(){
        banda.sumarRecaudacion(montoARecaudar())
    }

    fun montoARecaudar() = personaInvolucrada.ventasObtenidas  * porcentajeComisionRecoleccion

    override fun dineroAGanar() = montoARecaudar()

    override fun mensajeClave() = "La puerca está en la posilga"

    override fun monto() = montoARecaudar().toInt()
}

class abrirDeposito : Tarea(){
    var deposito = Deposito()
    var valorMetroCuadrado  : Double = 100.0

    //Primitiva
    override fun doRealizar(){
        Vito.pagar(costoCompra())
    }

    fun costoCompra() = valorMetroCuadrado * deposito.superficie

    override fun monto() = (valorMetroCuadrado * deposito.superficie).toInt()
}

class prestarDinero : Tarea(){
    var montoAPrestar : Double = 0.0
    var cantidadCuotas : Int = 4

    //Primitiva
    override fun doRealizar(){
        Vito.pagar(montoAPrestar)
        crearCuotas()
    }

    fun crearCuotas(){
        personaInvolucrada.sumarDeuda(montoADevolver())
        //(1..cantidadCuotas).forEach( Vito.agregarTarea(cobrarDinero))
    }

    fun valorCuota() = montoADevolver() / cantidadCuotas

    fun montoADevolver() = montoAPrestar * 2
}

class cobrarDinero : Tarea(){
    var dineroACobrar : Double = 0.0

    override fun doRealizar() {
        personaInvolucrada.bajarDeuda(dineroACobrar)
        banda.sumarRecaudacion(dineroACobrar)
    }

    override fun dineroAGanar() = dineroACobrar

}


data class Deposito(var superficie : Int = 0)

class Persona(){
    var ventasObtenidas : Double = 0.0
    var deuda : Double = 0.0

    fun sumarDeuda(monto: Double){
        deuda += monto
    }

    fun bajarDeuda(monto: Double){
        deuda -= monto
    }
}

open class Banda{
    var montoRecaudado : Double = 0.0
    val porcentajeComisionBanda : Double = 0.2
    var listaIntegrantes = mutableListOf<Integrante>()
    var lider = Integrante()

    fun sumarRecaudacion(monto : Double){
        var montoParaBanda = montoParaLaBanda(monto)
        montoRecaudado += montoParaLaBanda(monto)
        val montoParaVito = monto - montoParaBanda
        Vito.sumarRecaudacion(montoParaVito)
    }

    fun montoParaLaBanda(monto: Double) = porcentajeComisionBanda * monto

    fun puedeRealizar(tarea: Tarea) = !estaEnBancarrota() && puedeHacer(tarea)

    fun estaEnBancarrota() = montoRecaudado == 0.0

    open fun puedeHacer(tarea: Tarea) : Boolean = false

    fun agregarIntegrante(integrante: Integrante){
        listaIntegrantes.add(integrante)
    }

    fun eliminarIntegrante(integrante: Integrante){
        listaIntegrantes.remove(integrante)
    }

}

class BandaForajida : Banda() {
    override fun puedeHacer(tarea: Tarea) = listaIntegrantes.any{ it -> it.quiereHacer(tarea)}

}

class BandaTipica : Banda() {
    override fun puedeHacer(tarea: Tarea) = lider.quiereHacer(tarea)
}


class Vito{

    companion object {
        val bandas = mutableListOf<Banda>()
        val tareas = mutableListOf<Tarea>()
        var montoRecaudado : Double = 0.0

        fun sumarRecaudacion(monto: Double) {
            montoRecaudado += monto
        }

        fun pagar(monto: Double) {
            montoRecaudado -= monto
        }

        fun agregarTarea(tarea: Tarea){
            tareas.add(tarea)
        }

        fun asingarTareas(fecha: LocalDate){
            tareasPendientes(fecha).forEach{ tarea -> val bannda =
                bandas.first{ it -> it.puedeRealizar(tarea)}
                tarea.asignarBanda(bannda)
            }
        }
        fun tareasPendientes(fecha: LocalDate) = tareas.filter { it -> it.estaPendientePara(fecha) }

        fun realizarTareas(fecha: LocalDate){
            tareasPendientes(fecha).forEach{ it -> it.realizarTarea()}
        }
    }
}

class Integrante{
    lateinit var personalidad : Personalidad

    open fun quiereHacer(tarea : Tarea) = personalidad.quiereHacer(tarea)

}

interface Personalidad{
    fun quiereHacer(tarea: Tarea) : Boolean
}

class AltoPerfil : Personalidad{
    override fun quiereHacer(tarea: Tarea) = tarea.dineroAGanar() >= 1000.0

}

class Culposo : Personalidad {
    override fun quiereHacer(tarea: Tarea) = tarea.personaInvolucrada.ventasObtenidas > 5000
}

class Alternante : Personalidad {
    override fun quiereHacer(tarea: Tarea) = personalidad(tarea).quiereHacer(tarea)

    fun personalidad(tarea: Tarea): Personalidad = if (mesPar(tarea)) Culposo() else AltoPerfil()

    fun mesPar(tarea: Tarea) = tarea.fecha.monthValue % 2 == 0

}

// Composite
class Combinada : Personalidad {
    var personalidades = mutableListOf<Personalidad>()

    override fun quiereHacer(tarea: Tarea) = personalidades.all{ it -> it.quiereHacer(tarea) }

}


interface TareaRealizadaObserver{ //para el observer
    fun TareaRealizada(tarea: Tarea)
}

class WhatsAppObserver : TareaRealizadaObserver{ //para el observer
    // en este caso necesitamos el nro de Vitto que lo podemos tener aqui
    // palabra clave, lo vamos a tener en Tarea
    // monto se lo pedimos a tarea
    // crear notificacion en whatsapp Sender

    var numero : String = " 0 3 0 3 4 5 6"
    var pepe : String = ""
    lateinit var whatsAppSender : WhatAppSender

    override fun TareaRealizada(tarea: Tarea) {
        whatsAppSender.send(WhatsAppNotification(numero, tarea.mensajeClave() + " - " + tarea.monto() ))
        // mandamos al Mail Sender porque inyectamos una Dependencia, en este caso de tipo Setter Inyection.
    }
}

interface WhatAppSender{
    fun send(notificacion : WhatsAppNotification)
}

data class WhatsAppNotification(val mensaje: String, val destinatario: String)