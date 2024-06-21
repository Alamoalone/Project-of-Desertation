package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import vista.VistaPrincipal;
import vista.VistaAdd;
import vista.VistaEditar;

public class ControladorActionListener implements ActionListener{
	
	//referencia a la vista creando una variable
	 VistaPrincipal vistaPrincipal;
	 VistaAdd vistaAdd;
	 VistaEditar vistaEditar;
	 
	//Inicializar la variable en el constructor
	 
	public ControladorActionListener(VistaPrincipal vista) {
		this.vistaPrincipal = vista;
		
	}

	@Override
    public void actionPerformed(ActionEvent e) {
		//Aqui ponemos todo lo que hace click cuando le damos a los botones	
        
		//Al pulsar boton añadir de la principal, abre secundaria y llama a los liseners de VistaAdd
		if (e.getSource() == vistaPrincipal.getBotonAdd()) {
            vistaAdd = new VistaAdd(this);
            //Llamo a los listeners de VistaAdd
            vistaAdd.establecerListeners(this);
            vistaAdd.getCampoNombre().requestFocus();
        }
		
		//Al pulsar boton OK de añadir contacto, añade nombre y telefono a la tabla
		//else if (vistaAdd != null && e.getSource() == vistaAdd.getBotonOk()) {
		if (vistaAdd != null && e.getSource() == vistaAdd.getBotonOk()) {
            addDatosTabla();
          //  vistaAdd.setVisible(false);
        }
		
		//Al pulsar cancelar de añadir contacto cierra y queda la principal
		//else if (vistaAdd != null && e.getSource() == vistaAdd.getBotonCancel()) {
		if (vistaAdd != null && e.getSource() == vistaAdd.getBotonCancel()) {
           // vistaAdd.setVisible(false);
            vistaAdd.dispose();
        }
		
		//Al pulsar el boton editar de la tabla principal
		if (e.getSource() == vistaPrincipal.getBotonEdit()) {
			
            //vistaEditar.getCampoNombre().requestFocus();
            int selectedRow = vistaPrincipal.obtenerFilaSeleccionada();
            
    		if (selectedRow >= 0) {
			    vistaEditar = new VistaEditar(this);
			    vistaEditar.establecerListeners(this);
    			ponerDatosVistaEditar();
    		}else {
    			JOptionPane.showMessageDialog(null,"Tiene que seleccionar una fila", "Aviso", JOptionPane.INFORMATION_MESSAGE);	
    		}
                
        }
		
		//Al pulsar boton OK edita contacto, cambia nombre y/o telefono a la tabla
		//else if (vistaEditar != null && e.getSource() == vistaEditar.getBotonOk()) {
		if (vistaEditar != null && e.getSource() == vistaEditar.getBotonOk()) {
            //metodo editar
			vistaEditar.establecerListeners(this);
			editDatosTabla();
			vistaEditar.setVisible(false);
        }
		
		//Al pulsar cancelar de la vista editar, añadir contacto cierra y queda la principal
		//else if (vistaEditar != null && e.getSource() == vistaEditar.getBotonCancel()) {
		if (vistaEditar != null && e.getSource() == vistaEditar.getBotonCancel()) {
           // vistaAdd.setVisible(false);
			vistaEditar.establecerListeners(this);
			vistaEditar.dispose();
			//vistaAdd.setVisible(false);
        }
		
		//Al pulsar el boton borrar de la tabla principal
		if (e.getSource() == vistaPrincipal.getBotonDelete()) {
            vistaPrincipal.establecerListeners(this);
            eliminarContacto();
        }
    }
			

	//metodo para añadir los datos de los campos nombre y telefono a la tabla
	public void addDatosTabla() {
    	String nombre = vistaAdd.getCampoNombre().getText();
    	String telefono = vistaAdd.getCampoTelefono().getText();
    	if(nombre.isEmpty()){
    		JOptionPane.showMessageDialog(null,"Nombre esta vacio", "Aviso", JOptionPane.INFORMATION_MESSAGE);
    	}else if(telefono.isEmpty()){
    		JOptionPane.showMessageDialog(null,"Telefono esta vacio", "Aviso", JOptionPane.INFORMATION_MESSAGE);
    	}else {
    		DefaultTableModel tableModel = vistaPrincipal.getTableModel();
    		tableModel = vistaPrincipal.getTableModel();
            tableModel.addRow(new String[]{nombre, telefono});
            vistaAdd.setVisible(false);
    	}
    	
    }
	
	public void ponerDatosVistaEditar() {
		int selectedRow = vistaPrincipal.obtenerFilaSeleccionada();

		if (selectedRow >= 0) {
		    // Seleccionar la fila se encuentra en 'selectedRow'
		    // Puedes acceder a los datos de esa fila a través del modelo de tabla
		    String nombre = vistaPrincipal.getTableModel().getValueAt(selectedRow, 0).toString();
		    String telefono = vistaPrincipal.getTableModel().getValueAt(selectedRow, 1).toString();

	        // Asignar los valores a los campos de edición en vistaEditar
		    
	        vistaEditar.getCampoNombre().setText(nombre);
	        vistaEditar.getCampoTelefono().setText(telefono);
		}
		
	}
	public void editDatosTabla() {
		
		int filaSeleccionada = vistaPrincipal.obtenerFilaSeleccionada();

		if (filaSeleccionada >= 0) {
		   
	       // Asignar los valores a los campos de edición en vistaEditar
	        //Añadir los valores editados
	        String nombreEditado = vistaEditar.getCampoNombre().getText();
	    	String telefonoEditado = vistaEditar.getCampoTelefono().getText();
	    	if(nombreEditado.isEmpty()){
	    		JOptionPane.showMessageDialog(null,"Nombre esta vacio", "Aviso", JOptionPane.INFORMATION_MESSAGE);
	    	}else if(telefonoEditado.isEmpty()){
	    		JOptionPane.showMessageDialog(null,"Telefono esta vacio", "Aviso", JOptionPane.INFORMATION_MESSAGE);
	    	}else {
	    		DefaultTableModel tableModel = vistaPrincipal.getTableModel();
	    		tableModel = vistaPrincipal.getTableModel();
	    		tableModel.setValueAt(nombreEditado, filaSeleccionada, 0);
	    	    tableModel.setValueAt(telefonoEditado, filaSeleccionada, 1);
	    		
	    	}  
	        
        } else {
            // Mostrar un mensaje al usuario indicando que no hay fila seleccionada.
            JOptionPane.showMessageDialog(vistaPrincipal, "Selecciona un contacto para editar.", "Sin selección", JOptionPane.INFORMATION_MESSAGE);
        }

	}
	public void eliminarContacto() {
		int filaSeleccionada = vistaPrincipal.obtenerFilaSeleccionada();

		if (filaSeleccionada >= 0) {
			DefaultTableModel tableModel = vistaPrincipal.getTableModel();
			tableModel.removeRow(filaSeleccionada);
		}
		else {
            // Mostrar un mensaje al usuario indicando que no hay fila seleccionada.
            JOptionPane.showMessageDialog(vistaPrincipal, "Selecciona un contacto para editar.", "Sin selección", JOptionPane.INFORMATION_MESSAGE);
        }
		
	}
	 
	 
 
}
