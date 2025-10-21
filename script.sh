# !/bin/bash

# boucle sur les commandes à exécuter
for commandToLaunch in "$@"
do
    # exécution et récupération de la sortie de la commande
    commandOutput=$($commandToLaunch)

    echo "sortie de la commande : $commandOutput"

    while IFS= read -r match
    do
        if [[ match -ne "0" ]]
        then
            exit 1
        fi
    done < <(echo "$commandOutput" | grep -oE '"failed":[0-9]+' | grep -oE '[0-9]+') # redirection de la sortie dans le premier grep qui extrait 'failed: nombre' puis envoi du résultat dans le second pour récupérer le nombre
done

exit 0