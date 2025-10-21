import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Lanceur de commandes agilitest
 */
class CustomLauncher {
    /**
     * Lance les commandes agilitest dans l'ordre.
     * @param commands Commandes à exéctuer
     * @return 0 si chaque exécution réussie, sinon 1, si une des commandes échoue
     */
    protected int launchCommands(String... commands){
        for(String command : commands) {
            try{
                Process process = new ProcessBuilder( command.split(" ") ).start();
                process.waitFor();

                BufferedReader reader = new BufferedReader( new InputStreamReader( process.getErrorStream() ) );
                String line;

                while( ( line = reader.readLine() ) != null){
                    System.out.println("erreur ici : " + line);
                } 
            }
            catch(Exception e){
                System.out.println(e);
            }
        }

        return 0;
    }

    public static void main(String[] args) {
        CustomLauncher launcher = new CustomLauncher();
        
        System.exit( launcher.launchCommands(args) );
    }
}
