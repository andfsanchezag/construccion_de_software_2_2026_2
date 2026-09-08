package application.laptop;

import java.util.Objects;
import java.util.Objects;

public class Laptop implements Comparable<Laptop> {

    private String serial;
    private String marca;
    private String modelo;
    private int añoFabricacion;
    private String asignadoA;
    private boolean activo;

    public Laptop(String serial, String modelo) {
        this(serial, "Sin marca", modelo, 2022, "No asignado", true);
    }

    public Laptop(String serial, String marca, String modelo, int añoFabricacion, String asignadoA, boolean activo) {
        this.serial = serial;
        this.marca = marca;
        this.modelo = modelo;
        this.añoFabricacion = añoFabricacion;
        this.asignadoA = asignadoA;
        this.activo = activo;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAñoFabricacion() {
        return añoFabricacion;
    }

    public void setAñoFabricacion(int añoFabricacion) {
        this.añoFabricacion = añoFabricacion;
    }

    public String getAsignadoA() {
        return asignadoA;
    }

    public void setAsignadoA(String asignadoA) {
        this.asignadoA = asignadoA;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Laptop[ serial=" + serial + ", marca=" + marca + ", modelo=" + modelo + ", añoFabricacion=" + añoFabricacion + ", asignadoA=" + asignadoA + ", activo=" + activo + " ]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Laptop laptop = (Laptop) o;
        return añoFabricacion == laptop.añoFabricacion && Objects.equals(serial, laptop.serial) && Objects.equals(modelo, laptop.modelo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serial, modelo, añoFabricacion);
    }

    @Override
    public int compareTo(Laptop o) {
        return Integer.compare(o.añoFabricacion, this.añoFabricacion);
    }
}
