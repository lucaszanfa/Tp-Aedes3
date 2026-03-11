package DAO;

import Model.Registro;

public interface RegistroFactory<T extends Registro> {
    T create();
}
