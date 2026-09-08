package application.laptop;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Empresa {

    private String nombre;
    private int fundacion;
    private List<Laptop> laptops;

    public Empresa(String nombre, int fundacion) {
        this.nombre = nombre;
        this.fundacion = fundacion;
        this.laptops = new ArrayList<>();
    }

    public void agregarLaptop(Laptop l) {
        laptops.add(l);
    }

    public void agregarLaptopsDefecto(int repeticiones) {
        for (int i = 1; i <= repeticiones; i++) {
            String serial = String.format("LAP-%04d", i);
            Laptop laptop = new Laptop(serial, "Modelo Defecto");
            laptop.setMarca("Marca Defecto");
            laptop.setAñoFabricacion(2023);
            laptops.add(laptop);
        }
    }

    public Stream<String> laptopIds() {
        return laptops.stream().map(Laptop::getSerial);
    }

    public String getNombre() {
        return nombre;
    }

    public int getFundacion() {
        return fundacion;
    }

    public List<Laptop> getLaptops() {
        return laptops;
    }
}
