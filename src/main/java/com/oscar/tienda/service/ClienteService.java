package com.oscar.tienda.service;

import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    @Autowired
    ClienteRepository clienteRepository;

    public List<Cliente> listaClientes(){
        return clienteRepository.findAll();
    }

    public List<Cliente> buscarClienteNombre(String nombre){
        List<Cliente> clientes = clienteRepository.findByNombreIgnoreCase(nombre);
        if(clientes.isEmpty()){
            throw new RuntimeException("No se encontró ningún cliente con ese nombre");
        }
        return clientes;
    }

    public List<Cliente> buscarClientePorSimilitud(String nombre){
        List<Cliente> clientes = clienteRepository.findByNombreContainingIgnoreCase(nombre);
        if(clientes.isEmpty()){
            throw new RuntimeException("No se encontró ningún cliente con ese nombre");
        }
        return clientes;
    }

    public Cliente guardarCliente(Cliente cliente){
        // Validar duplicado ignorando mayúsculas
        boolean yaExiste = clienteRepository
                .findFirstByNombreIgnoreCase(cliente.getNombre().trim())
                .isPresent();
        if(yaExiste){
            throw new RuntimeException("Ya existe un cliente con el nombre: " + cliente.getNombre());
        }
        // Guardar con el nombre tal como se escribió
        cliente.setNombre(cliente.getNombre().trim());
        return clienteRepository.save(cliente);
    }

    public void eliminarCliente(Long id){
        if(!clienteRepository.existsById(id)){
            throw new RuntimeException("El cliente no existe");
        }
        clienteRepository.deleteById(id);
    }
}