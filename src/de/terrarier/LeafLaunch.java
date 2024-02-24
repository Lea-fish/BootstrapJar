package de.terrarier;

public class LeafLaunch {

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }

        // FIXME: for now just include the Leafish binary in the jar and run it (as a sub process)
        // FIXME: but in the future we would want to check the most recent version on github and
        // FIXME: download it if necessary
    }

}
