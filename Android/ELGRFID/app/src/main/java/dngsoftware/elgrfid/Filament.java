package dngsoftware.elgrfid;

import androidx.annotation.NonNull;

public class Filament {
    public int id;
    public String name;
    public int minTemp;
    public int maxTemp;

    public Filament(int id, String name, int minTemp, int maxTemp) {
        this.id = id;
        this.name = name;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}