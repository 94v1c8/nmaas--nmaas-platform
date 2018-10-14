package net.geant.nmaas.i18n.api;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Log4j2
public class MultiLanguageController {
    @GetMapping("/content/language/{language}")
    @ResponseStatus(HttpStatus.OK)
    public String getContents(@PathVariable("language") String language){
        String json = "";
        if(language.equalsIgnoreCase("en.json")){
            json = "{\"NAVBAR\":{\"MARKET\":\"Market\",\"SUBSCRIPTIONS\":\"Subscription\",\"INSTANCES\":\"Instances\",\"DOMAINS\":\"Domains\",\"USERS\":\"Users\",\"INVENTORY\":\"Inventory\",\"MONITOR\":\"Monitor\",\"CONFIGURATION\":\"Configuration\",\"PROFILE\":\"Profile\",\"LOGOUT\":\"Logout\",\"LOGIN_REGISTER\":\"Login/Register\"},\"WELCOME\":{\"LOGIN\":\"Login\",\"REGISTER\":\"Register\",\"TITLE1\":\"Network Management as a Service\",\"TITLE2\":\"provides a portfolio of network management applications run on a per-user, secured network monitoring infrastructure.\",\"P1\":\"GÉANT’s NMaaS service includes three aspects: providing, managing and maintaining the infrastructure of the NMaaS service portal, platform and selected tools, supporting users in using the system, and the selected tools for monitoring their networks via NMaaS, as well as supporting users that contribute their software to NMaaS system.\",\"P2\":\"Users\",\"P3\":\"NMaaS users are organisations that do not want to own NMS infrastructure themselves and/or want to outsource network management, as well as organisations and/or individuals that are searching for quality network management software or who want to share their software within the community.\",\"P4\":\"System, marketplace and service\",\"P5\":\"NMaaS provides multiple benefits, as a system, an application marketplace, and as a service. NMaaS simplifies domain network management by providing the infrastructure and tools via a cloud-based, multi-tenant and secure network management system. It enables the deployment of network management tools, as well as management and monitoring of client networks, although NMaaS does not monitor and manage the network by itself. The selection of tools that is and/or can be made available to users is not limited to network management tools and can be easily extended.\",\"P6\":\"Cost reduction\",\"P7\":\"With NMaaS, users do not have to bear the ownership costs and risks related to maintenance and control of the network management infrastructure.\",\"P8\":\"Smart scaling\",\"P9\":\"If there is a need to extend Your services - you can do it within few minutes in any place, any time, by just deploying new instances\",\"P10\":\"NMaaS Features\",\"P11\":\"Application Market\",\"P12\":\"At store you can find not only official applications, selected by the administrator, but also there is a place for applications created by community - and to share them with other people.\",\"P13\":\"Rating system\",\"P14\":\"You want to share your opinion about application? Give some hints to author? There is solution - you can rate and comment every application at store.\",\"P15\":\"SSO Support\",\"P16\":\"If you want to login using same account for few services, not only NMaasS, You can login using SSO, where you can choose your identity provider and login using existing account.\",\"P17\":\"Fast & easy deployment\",\"P18\":\"You can deploy your first application in few minutes - just add your configuration, get application from Market and deploy it - our services will do rest. In few minutes you will get address for your instance of application.\",\"P19\":\"Easy scaling\",\"P20\":\"More networks, more domains? That's simple - you can easily scale horizontal applications by adding more instances of application.\",\"P21\":\"Domains management\",\"P22\":\"You have more then one domain to manage, but you don't have own infrastructure? There is simple solution - NMaaS support multiple domains and configurations to manage.\",\"P23\":\"Technologies used\"},\"REGISTRATION\":{\"USERNAME\":\"Username\",\"PASSWORD\":\"Password\",\"CONFIRM_PASSWORD\":\"Confirm password\",\"EMAIL\":\"Email\",\"FIRST_NAME\":\"First name\",\"LAST_NAME\":\"Last name\",\"DOMAIN\":\"Domain\",\"NOT_SELECTED\":\"Not selected\",\"TERMS\":\"Terms of Use\",\"AGREEMENT_MESSAGE\":\"I agree to the\",\"PRIVACY_POLICY\":\"Privacy Policy\",\"SUBMIT\":\"Submit\",\"FORM_SUBMISSION\":\"Registration form submission\",\"THANK_YOU_MESSAGE\":\"Thank you for submitting the registration form. Your account needs to be enabled by the system administrator. This make take some time.\",\"DONE_MESASAGE\":\"Done\"},\"LOGIN\":{\"USERNAME\":\"Username\",\"USERNAME_REQUIRED_MESSAGE\":\"Username is required\",\"PASSWORD\":\"Password\",\"PASSWORD_REQUIRED_MESSAGE\":\"Password is required\",\"LOGIN\":\"Login\",\"LOGIN_WITH\":\"Login with\"},\"CLUSTERS\":{\"TITLE\":\"Kubernetes cluster configuration\",\"HELM\":\"Helm\",\"HOST_ADDRESS\":\"REST Host address\",\"HOST_SSH_USERNAME\":\"Host SSH username\",\"USER_LOCAL_CHART_ARCHIEVES\":\"Use local chart archives\",\"HEML_CHART_REPO_NAME\":\"Helm chart repository name\",\"HEML_CHARTS_DIRECTORY\":\"Helm charts directory\",\"REST_API\":\"REST API\",\"USER_REST_API\":\"Use REST API\",\"HOST_PORT\":\"Host port\",\"INGRESS\":\"Ingress\",\"CONTROLLER_CONFIG_OPTION\":\"Controller config option\",\"SUPPORTED_INGRESS_CLASS\":\"Supported Ingress class\",\"CONTROLLER_CHART_NAME\":\"Controller chart name\",\"CONTROLLER_CHART_ARCHIEVE\":\"Controller chart archive\",\"RESOURCE_CONFIG_OPTION\":\"Resource config option\",\"EXTERNAL_SERVICE_DOMAIN\":\"External service domain\",\"TLS_SUPPORTED\":\"TLS supported\",\"DEPLOYMENT\":\"Deployment\",\"NAMESPACE_CONFIG_OPTION\":\"Namespace config option\",\"DEFAULT_NAMESPACE\":\"Default namespace\",\"DEFAULT_STORAGE_CLASS\":\"Default storage class\",\"USER_LOCAL_GITLAB_INSTANCE\":\"Use local GitLab instance\",\"ATTACH_POINT\":\"Attach point\",\"ROUTER_NAME\":\"Router name\",\"ROUTER_ID\":\"Router ID\",\"ROUTER_INTERFACE_NAME\":\"Router interface name\",\"EXTERNAL_NETWORKS\":\"External networks\",\"ADDRESS\":\"Address\",\"NETWORK\":\"Network\",\"NETMASK_LENGTH\":\"Netmask length\",\"ASSIGNED\":\"Assigned\",\"ASSIGNED_SINCE\":\"Assigned since\",\"ASSIGNED_TO\":\"Assigned to\",\"ADD_BUTTON\":\"Add\",\"VIEW_BUTTON\":\"View\",\"EDIT_BUTTON\":\"Edit\",\"REMOVE_BUTTON\":\"Remove\",\"SUBMIT_BUTTON\":\"Submit\",\"HOST_SSH_USERNAME_VALIDATION_MESSAGE\":\"Helm host address must contain only digits and dots\",\"HOST_ADDRESS_REQUIRED_MESSAGE\":\"Helm host address is required\",\"HOST_ADDRESS_VALIDATION_MESSAGE\":\"Helm host address must contain only digits and dots\",\"HOST_SSH_USERNAME_REQUIRED_MESSAGE\":\"Host SSH username is required\",\"HELM_CHART_REPO_NAME_REQUIRED_MESSAGE\":\"Helm chart repository name is required\",\"HELM_CHART_DIRECTORY_REQUIRED_MESSAGE\":\"Helm charts directory is required\",\"REST_API_HOST_ADDRESS_REQUIRED_MESSAGE\":\"REST API host address is required\",\"REST_API_HOST_ADDRESS_VALIDATION_MESSAGE\":\"REST API host address must contain only numbers and dots\",\"REST_API_PORT_REQUIRED_MESSAGE\":\"REST API host port is required\",\"IP_REQUIRED_MESSAGE\":\"IP is required\",\"IP_VALIDATION_MESSAGE\":\"IP must contain only digits and dots\",\"NETWORK_REQUIRED_MESSAGE\":\"Network is required\",\"NETWORK_ADDRESS_VALIDATION_MESSAGE\":\"Network address must contain only digits and dots\",\"MASK_VALIDATION_MESSAGE\":\"Mask must be between 0 and 32\"},\"GIT_LAB\":{\"TITLE\":\"GitLab configuration\",\"SERVER_ADDRESS\":\"Server address\",\"SERVER_ADDRESS_REQUIRED_MESSAGE\":\"Server address is required\",\"PORT\":\"Port\",\"PORT_REQUIRED_MESSAGE\":\"Port is required\",\"SSH_SERVER\":\"Ssh server\",\"SSH_SERVER_REQUIRED_MESSAGE\":\"Ssh server is required\",\"TOKEN\":\"Token\",\"TOKEN_REQUIRED_MESSAGE\":\"Token is required\",\"REPO_ACCESS_USERNAME\":\"Repository access username\",\"REPO_ACCESS_USERNAME_REQUIRED_MESSAGE\":\"Repository access username is required\",\"VIEW_BUTTON\":\"View\",\"SUBMIT_BUTTON\":\"Submit\",\"EDIT_BUTTON\":\"Edit\"},\"SAML_PROXY\":{\"TITLE\":\"SAML Proxy configuration\",\"LOGIN_URL\":\"Login url\",\"LOGOUT_URL\":\"Logout url\",\"SSO_KEY_FILE_PATH\":\"SSO key file path\",\"TIMEOUT\":\"Timeout [minutes]\",\"VIEW_BUTTON\":\"View\",\"SUBMIT_BUTTON\":\"Submit\",\"EDIT_BUTTON\":\"Edit\",\"LOGIN_URL_REQUIRED_MESSAGE\":\"Login url is required\",\"LOGOUT_URL_REQUIRED_MESSAGE\":\"Logout url is required\",\"SSO_KEY_REQUIRED_MESSAGE\":\"SSO key is required\",\"TIMEOUT_REQUIRED_MESSAGE\":\"Timeout is required\"},\"USERS\":{\"TITLE\":\"Users\",\"USER_NAME\":\"Username\",\"DOMAINS\":\"Domains\",\"GLOBAL_ROLE\":\"Global role\",\"ROLES\":\"Roles\",\"ENABLED\":\"Enabled\",\"ENABLED_BUTTON\":\"Enabled\",\"DETAILS_BUTTON\":\"Details\",\"DISABLE_BUTTON\":\"Disable\",\"REMOVE_FROM_DOMAIN_BUTTON\":\"Remove from domain\"},\"DOMAINS\":{\"TITLE\":\"Domains\",\"CODE_NAME\":\"Codename\",\"NAME\":\"Name\",\"ACTIVATE\":\"Active\",\"DETAILS_BUTTON\":\"Details\",\"EDIT_BUTTON\":\"Edit\",\"REMOVE_BUTTON\":\"Remove\"},\"APP_INSTANCES\":{\"TITLE\":\"Application instances\",\"SHOW\":\"Show\",\"NAME\":\"Name\",\"DOMAIN\":\"Domain\",\"OWNER\":\"Owner\",\"DEPLOYED_AT\":\"Deployed at\",\"APPLICATION\":\"Application\",\"STATE\":\"State\",\"DETAILS_BUTTON\":\"Details\",\"GO_TO_APP_BUTTON\":\"Go to app\"}}";
        }
        if(language.equalsIgnoreCase("fr.json")){
            json = "{\"NAVBAR\":{\"MARKET\":\"MarchÃƒÂ©\",\"SUBSCRIPTIONS\":\"Abonnement\",\"INSTANCES\":\"Instances\",\"DOMAINS\":\"Domaines\",\"USERS\":\"Utilisateurs\",\"INVENTORY\":\"Inventaire\",\"MONITOR\":\"Moniteur\",\"CONFIGURATION\":\"Configuration\",\"PROFILE\":\"Profil\",\"LOGOUT\":\"Connectez - Out\",\"LOGIN_REGISTER\":\"Connexion / S'inscrire\"},\"WELCOME\":{\"LOGIN\":\"S'identifier\",\"REGISTER\":\"Registre\",\"TITLE1\":\"Gestion de rÃ©seau en tant que service\",\"TITLE2\":\"Fournit un portefeuille d'applications de gestion de rÃ©seau exÃ©cutÃ©es sur une infrastructure de surveillance rÃ©seau sÃ©curisÃ©e par utilisateur.\",\"P1\":\"GÃ‰ANTâ€™s NMaaS service comprend trois aspects: la fourniture, la gestion et la maintenance de l'infrastructure du portail de service NMaaS, la plate-forme et les outils sÃ©lectionnÃ©s, le soutien des utilisateurs dans l'utilisation du systÃ¨me, et les outils sÃ©lectionnÃ©s pour la surveillance de leurs rÃ©seaux Via NMaaS, ainsi que de soutenir les utilisateurs qui apportent leur logiciel Ã  NMaaS systÃ¨me.\",\"P2\":\"Utilisateurs\",\"P3\":\"Les utilisateurs de NMaaS sont des organisations qui ne veulent pas possÃ©der les infrastructures des NEM elles-mÃªmes et/ou qui veulent externaliser la gestion de rÃ©seau, ainsi que des organisations et/ou des individus qui recherchent un logiciel de gestion de rÃ©seau de qualitÃ© ou qui veulent partager leur logiciel au sein de la communautÃ©.\",\"P4\":\"SystÃ¨me, marchÃ© et service\",\"P5\":\"NMaaS fournit de multiples avantages, en tant que systÃ¨me, un marchÃ© d'applications, et en tant que service. NMaaS simplifie la gestion de rÃ©seau de domaine en fournissant l'infrastructure et les outils par l'intermÃ©diaire d'un nuage-basÃ©, multi-locataire et systÃ¨me sÃ©curisÃ© de gestion de rÃ©seau. Il permet le dÃ©ploiement d'outils de gestion de rÃ©seau, ainsi que la gestion et la surveillance des rÃ©seaux clients, bien que NMaaS ne surveille pas et ne gÃ¨re pas le rÃ©seau par lui-mÃªme. La sÃ©lection des outils qui sont et/ou peuvent Ãªtre mis Ã  la disposition des utilisateurs ne se limite pas aux outils de gestion de rÃ©seau et peut Ãªtre facilement Ã©tendue.\",\"P6\":\"RÃ©duction des coÃ»ts\",\"P7\":\"Avec NMaaS, les utilisateurs n'ont pas Ã  supporter les coÃ»ts de propriÃ©tÃ© et les risques liÃ©s Ã  la maintenance et au contrÃ´le de l'infrastructure de gestion de rÃ©seau.\",\"P8\":\"Mise Ã  l'Ã©chelle intelligente\",\"P9\":\"S'il est nÃ©cessaire d'Ã©tendre vos services-vous pouvez le faire en quelques minutes dans n'importe quel endroit, n'importe quand, en dÃ©ployant simplement de nouvelles instances\",\"P10\":\"NMaaS features\",\"P11\":\"MarchÃ© de l'Application\",\"P12\":\"En magasin vous pouvez trouver non seulement les applications officielles, sÃ©lectionnÃ©es par l'administrateur, mais aussi il ya une place pour les applications crÃ©Ã©es par la communautÃ©-et de les partager avec d'autres personnes.\",\"P13\":\"SystÃ¨me de cotation\",\"P14\":\"Vous voulez partager votre opinion sur l'application? Donner quelques conseils Ã  L'auteur? Il y a une solution-vous pouvez noter et commenter chaque application en magasin.\",\"P15\":\"Support SSO\",\"P16\":\"Si vous voulez vous connecter en utilisant le mÃªme compte pour peu de services, non seulement NMaasS, vous pouvez vous connecter en utilisant SSO, oÃ¹ vous pouvez choisir votre fournisseur d'identitÃ© et de connexion en utilisant le compte existant.\",\"P17\":\"DÃ©ploiement rapide et facile\",\"P18\":\"Vous pouvez dÃ©ployer votre premiÃ¨re application en quelques minutes-il suffit d'ajouter votre configuration, obtenir l'application du marchÃ© et de le dÃ©ployer-nos services se reposer. En quelques minutes, vous obtiendrez l'adresse de votre instance d'application.\",\"P19\":\"Mise Ã  l'Ã©chelle aisÃ©e\",\"P20\":\"Plus de rÃ©seaux, plus de domaines? C'est simple: vous pouvez facilement mettre Ã  l'Ã©chelle des applications horizontales en ajoutant d'autres instances d'application.\",\"P21\":\"Gestion de domaines\",\"P22\":\"Vous avez plus d'un domaine Ã  gÃ©rer, mais vous n'avez pas d'infrastructure propre? Il existe une solution simple-NMaaS prendre en charge plusieurs domaines et configurations Ã  gÃ©rer.\",\"P23\":\"Technologies utilisÃ©es\"},\"REGISTRATION\":{\"USERNAME\":\"Nom d'utilisateur\",\"PASSWORD\":\"Mot de passe\",\"CONFIRM_PASSWORD\":\"Confirmez le mot de passe\",\"EMAIL\":\"Email\",\"FIRST_NAME\":\"Prénom\",\"LAST_NAME\":\"Nom de famille\",\"DOMAIN\":\"Domain\",\"NOT_SELECTED\":\"Non séléctionné\",\"TERMS\":\"Conditions d'utilisation\",\"AGREEMENT_MESSAGE\":\"je suis d'accord avec le\",\"PRIVACY_POLICY\":\"Politique de confidentialité\",\"SUBMIT\":\"Soumettre\",\"FORM_SUBMISSION\":\"Formulaire d'inscription\",\"THANK_YOU_MESSAGE\":\"Merci d'avoir envoyé le formulaire d'inscription. Votre compte doit être activé par l'administrateur système. Cela prend du temps.\",\"DONE_MESASAGE\":\"Terminé\"},\"LOGIN\":{\"USERNAME\":\"Nom d'utilisateur\",\"USERNAME_REQUIRED_MESSAGE\":\"Le nom d'utilisateur est requis\",\"PASSWORD\":\"Mot de passe\",\"PASSWORD_REQUIRED_MESSAGE\":\"Le mot de passe est requis\",\"LOGIN\":\"S'identifier\",\"LOGIN_WITH\":\"Connectez-vous avec\"},\"CLUSTERS\":{\"TITLE\":\"Configuration du cluster Kubernetes\",\"HELM\":\"Helm\",\"HOST_ADDRESS\":\"Adresse de l'hôte REST\",\"HOST_SSH_USERNAME\":\"Nom d'utilisateur de l'hôte SSH\",\"HOST_SSH_USERNAME_VALIDATION_MESSAGE\":\"adresse hôte de barre ne doit contenir que des chiffres et des points\",\"USER_LOCAL_CHART_ARCHIEVES\":\"Utiliser les archives de graphique locales\",\"HEML_CHART_REPO_NAME\":\"Nom du référentiel du diagramme de pilotage\",\"HEML_CHARTS_DIRECTORY\":\"Répertoire des cartes de pilotage\",\"REST_API\":\"API REST\",\"HOST_PORT\":\"Host port\",\"USER_REST_API\":\"Utiliser l'API REST\",\"INGRESS\":\"Ingress\",\"CONTROLLER_CONFIG_OPTION\":\"Option de configuration du contrôleur\",\"SUPPORTED_INGRESS_CLASS\":\"Classe d'entrée prise en charge\",\"CONTROLLER_CHART_NAME\":\"Nom du graphique du contrôleur\",\"CONTROLLER_CHART_ARCHIEVE\":\"Archive du graphique du contrôleur\",\"RESOURCE_CONFIG_OPTION\":\"Option de configuration de ressource\",\"EXTERNAL_SERVICE_DOMAIN\":\"Domaine de service externe\",\"TLS_SUPPORTED\":\"TLS supporté\",\"DEPLOYMENT\":\"Deployment\",\"NAMESPACE_CONFIG_OPTION\":\"Option de configuration d'espace de noms\",\"DEFAULT_NAMESPACE\":\"Espace de nom par défaut\",\"DEFAULT_STORAGE_CLASS\":\"Classe de stockage par défaut\",\"USER_LOCAL_GITLAB_INSTANCE\":\"Utiliser l'instance GitLab locale\",\"ATTACH_POINT\":\"Attach point\",\"ROUTER_NAME\":\"Nom du routeur\",\"ROUTER_ID\":\"ID de routeur\",\"ROUTER_INTERFACE_NAME\":\"Nom de l'interface du routeur\",\"EXTERNAL_NETWORKS\":\"Réseaux externes\",\"ADDRESS\":\"Adresse\",\"NETWORK\":\"Réseau\",\"NETMASK_LENGTH\":\"Longueur du masque de réseau\",\"ASSIGNED\":\"Assigned\",\"ASSIGNED_SINCE\":\"Attribué depuis\",\"ASSIGNED_TO\":\"Assigné à\",\"ADD_BUTTON\":\"Ajouter\",\"VIEW_BUTTON\":\"View\",\"EDIT_BUTTON\":\"Edit\",\"SUBMIT_BUTTON\":\"Soumettre\",\"REMOVE_BUTTON\":\"Retirer\",\"HOST_ADDRESS_REQUIRED_MESSAGE\":\"L'adresse de l'hôte est requise\",\"HOST_ADDRESS_VALIDATION_MESSAGE\":\"adresse hôte de barre ne doit contenir que des chiffres et des points\",\"HOST_SSH_USERNAME_REQUIRED_MESSAGE\":\"Le nom d'utilisateur pour l'hôte SSH est requis\",\"HELM_CHART_REPO_NAME_REQUIRED_MESSAGE\":\"Le nom du référentiel de diagrammes de barre est requis\",\"HELM_CHART_DIRECTORY_REQUIRED_MESSAGE\":\"Le répertoire des graphiques de barre est requis\",\"REST_API_HOST_ADDRESS_REQUIRED_MESSAGE\":\"L'adresse de l'hôte de l'API REST est requise\",\"REST_API_HOST_ADDRESS_VALIDATION_MESSAGE\":\"L'adresse de l'hôte de l'API REST doit contenir uniquement des chiffres et des points\",\"REST_API_PORT_REQUIRED_MESSAGE\":\"Le port d'hôte de l'API REST est requis\",\"IP_REQUIRED_MESSAGE\":\"IP est requis\",\"IP_VALIDATION_MESSAGE\":\"IP ne doit contenir que des chiffres et des points\",\"NETWORK_REQUIRED_MESSAGE\":\"Le réseau est requis\",\"NETWORK_ADDRESS_VALIDATION_MESSAGE\":\"L'adresse réseau doit contenir uniquement des chiffres et des points\",\"MASK_VALIDATION_MESSAGE\":\"Le masque doit être compris entre 0 et 32\"},\"GIT_LAB\":{\"TITLE\":\"Configuration de GitLab\",\"SERVER_ADDRESS\":\"Adresse du serveur\",\"SERVER_ADDRESS_REQUIRED_MESSAGE\":\"L'adresse du serveur est requise\",\"PORT\":\"Port\",\"PORT_REQUIRED_MESSAGE\":\"Le port est requis\",\"SSH_SERVER\":\"serveur Ssh\",\"SSH_SERVER_REQUIRED_MESSAGE\":\"Le serveur Ssh est requis\",\"TOKEN\":\"Jeton\",\"TOKEN_REQUIRED_MESSAGE\":\"Un jeton est requis\",\"REPO_ACCESS_USERNAME\":\"Nom d'utilisateur d'accès au référentiel\",\"REPO_ACCESS_USERNAME_REQUIRED_MESSAGE\":\"Le nom d'utilisateur d'accès au référentiel est requis\",\"VIEW_BUTTON\":\"View\",\"SUBMIT_BUTTON\":\"Soumettre\",\"EDIT_BUTTON\":\"Modifier\"},\"SAML_PROXY\":{\"TITLE\":\"Configuration du proxy SAML\",\"LOGIN_URL\":\"URL de connexion\",\"LOGOUT_URL\":\"Logout url\",\"SSO_KEY_FILE_PATH\":\"chemin du fichier de clé SSO\",\"TIMEOUT\":\"Timeout [minutes]\",\"VIEW_BUTTON\":\"View\",\"SUBMIT_BUTTON\":\"Soumettre\",\"EDIT_BUTTON\":\"Modifier\",\"LOGIN_URL_REQUIRED_MESSAGE\":\"L'URL de connexion est obligatoire\",\"LOGOUT_URL_REQUIRED_MESSAGE\":\"L'URL de déconnexion est requise\",\"SSO_KEY_REQUIRED_MESSAGE\":\"La clé SSO est requise\",\"TIMEOUT_REQUIRED_MESSAGE\":\"Le délai d'attente est requis\"},\"USERS\":{\"TITLE\":\"Utilisateurs\",\"USER_NAME\":\"Nom d'utilisateur\",\"DOMAINS\":\"Domaines\",\"GLOBAL_ROLE\":\"Rôle global\",\"ROLES\":\"Roles\",\"ENABLED\":\"Enabled\",\"ENABLED_BUTTON\":\"Activé\",\"DETAILS_BUTTON\":\"Détails\",\"DISABLE_BUTTON\":\"Disable\",\"REMOVE_FROM_DOMAIN_BUTTON\":\"Supprimer du domaine\"},\"DOMAINS\":{\"TITRE\":\"Domaines\",\"CODE_NAME\":\"Nom de code\",\"NAME\":\"Nom\",\"ACTIVATE\":\"Actif\",\"DETAILS_BUTTON\":\"Détails\",\"EDIT_BUTTON\":\"Modifier\",\"REMOVE_BUTTON\":\"Supprimer\"},\"APP_INSTANCES\":{\"TITLE\":\"Instances d'application\",\"SHOW\":\"Montre\",\"NAME\":\"Nom\",\"DOMAIN\":\"Domaine\",\"OWNER\":\"Propriétaire\",\"DEPLOYED_AT\":\"Deployed at\",\"APPLICATION\":\"Application\",\"STATE\":\"State\",\"DETAILS_BUTTON\":\"Détails\",\"GO_TO_APP_BUTTON\":\"Aller à l'application\"}}";
        }
        if(language.equalsIgnoreCase("pl.json")){
            json = "{\"NAVBAR\":{\"MARKET\":\"Rynek\",\"SUBSCRIPTIONS\":\"Subskrypcja\",\"INSTANCES\":\"Instancje\",\"DOMAINS\":\"Domeny\",\"USERS\":\"Users\",\"INVENTORY\":\"Użytkownicy\",\"MONITOR\":\"Monitor\",\"CONFIGURATION\":\"Konfiguracja\",\"PROFILE\":\"Profil\",\"LOGOUT\":\"Wyloguj\",\"LOGIN_REGISTER\":\"Zaloguj się / Zarejestruj się\"},\"WELCOME\":{\"LOGIN\":\"Zaloguj Się\",\"REGISTER\":\"Zarejestrować\",\"TITLE1\":\"Kierownictwo Sieci jak (ponieważ) Służba (serwis)\",\"TITLE2\":\"zaopatruje tekę użytków (podanie) kierownictwa sieci biec (przesuwać się; przesunął się; działanie; przesunięty) na na użytkownika, zapewnił (zapewniony) sieć kontrolujący system urządzeństanowiący osnowę.\",\"P1\":\"GEANT's *NMaaS* obsługiwać (służba; serwis) włącza trzy aspekty: zaopatrywanie, dający sobie radę (kierowniczy; zarządzający; kierowanie) i zachowujący (utrzymujący) system urządzeństanowiący osnowę *NMaaS* obsługują portalowy, platforma i dobrał (dobrany) narzędzie (obrabia), popierający (popieranie) użytkownicy w używaniu systemu, i dobierane narzędzia dla kontroli ich sieci przez *NMaaS*, jak i popierający użytkownicy które przyczyniają się (wnosić) ich program komputerowy *NMaaS* system.\",\"P2\":\"Użytkownicy\",\"P3\":\"NMaaS* użytkownicy są organizacje które nie potrzebują (chcieć) żeby posiadać *NMS* system urządzeństanowiący osnowę s& (samodzielnie) i /albo brak (potrzeba; potrzebować; chcieć) *outsource* kierownictwo sieci, jak i organizacje i /albo jednostki które szukają kierownictwa sieci jakości programu komputerowego albo kto (który) brak (potrzeba; potrzebować; chcieć) rozdzielać ich program komputerowy w obrębie wspólnoty.\",\"P4\":\"System, rynek i służba (serwis)\",\"P5\":\"NMaaS* zaopatruje wielokrotne korzyści, jak (ponieważ) system, stosowany rynek, i jak (ponieważ) służba (serwis). *NMaaS* upraszcza kierownictwo sieci dziedziny przez zaopatrywanie systemu urządzeństanowiącego osnowę i narzędzie (obrabia) przez na bazie chmura, wieledzierżawca i pewna sieć system kierownictwa. To umożliwia rozwijęciu kierownictwa sieci narzędzie (obrabia), jak i kierownictwo i kontrolujący klienta sieci, chociaż *NMaaS* nie kontroluje i kierują (dawać sobie radę) siecią z natury. wybór narzędzi które jest i /albo może jest ułatwiał dostęp do użytkowników nie jest ograniczany kierownictwem sieci narzędzie (obrabia) i może jest łatwo rozprzestrzenił (rozprzestrzeniony; przedłużył; przedłużony).\",\"P6\":\"Kosztować (koszt; kosztował; oceniony) skrócenie\",\"P7\":\"Z *NMaaS*, użytkownicy robi nie musi nieść koszty własności i ryzyk& związany z utrzymaniem i kierowanie (kontrolować; kontrolowany; kontrola) kierownictwa sieci systemu urządzeństanowiącego osnowę.\",\"P8\":\"Piekące ważenie (obliczanie)\",\"P9\":\"Jeżeli tam jest zapotrzebowanie rozprzestrzeniać Wasze usługi - wy możecie robić to w obrębie niewielu minut w wszelkim miejscu, wszelki czas, przez po prostu (dopiero co) rozwijający (rozwijanie) nowe wypadki\",\"P10\":\"NMaaS* Cecha (cechuje; przedstawia)\",\"P11\":\"Użytek (podanie; stosowany) Handlować (rynek; rynkowy; popyt)\",\"P12\":\"Przy zasobie (sklep) wy możecie znajdować nie tylko osoba urzędowa (oficjalny) użytki (podanie), dobierany przez administratora, ale także tam jest miejsce dla użytków (podanie) stworzonych przez wspólnotę - i rozdzielać & (oni) z innym ludzie (lud; ludowy).\",\"P13\":\"Oceniający (ocena; uważający) system\",\"P14\":\"Wy potrzebujecie (chcieć) żeby rozdzielać waszą opinię o użytku (podanie)? Dają niektóre aluzje do autora? Tam jest rozwiązanie - wy możecie oceniać i komentują każdy użytek (podanie) przy zasobie (sklep).\",\"P15\":\"SSO* Popierają\",\"P16\":\"Jeżeli wy potrzebujecie (chcieć) *login* używający (używanie; przyzwyczajający się) ten sam rachunek (wyjaśnienie; z powodu) dla niewielu usług, nie tylko *NMaasS*, Wy może *login* używający (używanie; przyzwyczajający się) *SSO*, gdzie wy możecie wybierać waszego dostawcę identyczności i *login* używający (używanie; przyzwyczajający się) istniejący (istnienie) rachunek (wyjaśnienie; z powodu; odpowiadać).\",\"P17\":\"Szybki (szybko) & proste rozwijęcie\",\"P18\":\"Wy możecie rozwijać wasz pierwszy (najpierw) użytek (podanie) w niewielu minutach - po prostu (dopiero co) dodają waszą konfigurację, otrzymują (dostawać; rozumieć) użytek (podanie) od Rynek (rynkowy; popyt) i rozwijają to - nasze usługi będzie robić odpoczywać (pozostawać; odpoczynek; reszta). W niewielu minutach wy będziecie otrzymywać (dostawać; rozumieć) adres dla waszego wypadku użytku (podanie).\",\"P19\":\"Proste ważenie (obliczanie)\",\"P20\":\"Większa ilość (bardziej; więcej) sieci, większa ilość (bardziej; więcej) dziedziny? Który jest prosty - wy łatwo możecie ważyć (obliczać) poziome użytki (podanie) przez dodawanie większa ilość (bardziej; więcej) wypadki użytku (podanie).\",\"P21\":\"Kierownictwo Dziedzin\",\"P22\":\"Wy ma bardziej (więcej) potem jedzicie dziedzina kierować (dawać sobie radę), ale wy nie macie własne system urządzeństanowiący osnowę? Tu jest prostym rozwiązaniem - *NMaaS* popierają wielokrotne dziedziny i konfiguracje kierować (dawać sobie radę).\",\"P23\":\"Technologie używał\"},\"REGISTRATION\":{\"USERNAME\":\"Nazwa Użytkownika\",\"PASSWORD\":\"Hasło\",\"CONFIRM_PASSWORD\":\"Potwierdź hasło\",\"EMAIL\":\"E-mail\",\"FIRST_NAME\":\"Imię\",\"LAST_NAME\":\"Nazwisko\",\"DOMAIN\":\"Domain\",\"NOT_SELECTED\":\"Nie zaznaczone\",\"TERMS\":\"Warunki korzystania\",\"AGREEMENT_MESSAGE\":\"zgadzam się z\",\"PRIVACY_POLICY\":\"Polityka prywatności\",\"SUBMIT\":\"Zatwierdź\",\"FORM_SUBMISSION\":\"Przesłanie formularza rejestracyjnego\",\"THANK_YOU_MESSAGE\":\"Dziękujemy za przesłanie formularza rejestracyjnego. Twoje konto musi zostać włączone przez administratora systemu. To zajmuje trochę czasu.\",\"DONE_MESASAGE\":\"Gotowe\"},\"LOGIN\":{\"USERNAME\":\"Nazwa użytkownika\",\"USERNAME_REQUIRED_MESSAGE\":\"Nazwa użytkownika jest wymagana\",\"PASSWORD\":\"Hasło\",\"PASSWORD_REQUIRED_MESSAGE\":\"Wymagane jest hasło\",\"LOGIN\":\"Zaloguj Się\",\"LOGIN_WITH\":\"Zaloguj się z\"},\"CLUSTERS\":{\"TITLE\":\"Konfiguracja klastra Kubernetes\",\"HELM\":\"Helm\",\"HOST_ADDRESS\":\"Adres hosta REST\",\"HOST_SSH_USERNAME\":\"Nazwa hosta SSH\",\"HOST_SSH_USERNAME_VALIDATION_MESSAGE\":\"Adres hosta kontrolnego musi zawierać tylko cyfry i kropki\",\"USER_LOCAL_CHART_ARCHIEVES\":\"Użyj lokalnych archiwów wykresów\",\"HEML_CHART_REPO_NAME\":\"Nazwa repozytorium wykresu hełmu\",\"HEML_CHARTS_DIRECTORY\":\"Katalog map hełmu\",\"REST_API\":\"REST API\",\"HOST_PORT\":\"Host port\",\"USER_REST_API\":\"Użyj interfejsu API REST\",\"INGRESS\":\"Ingress\",\"CONTROLLER_CONFIG_OPTION\":\"Opcja konfiguracji sterownika\",\"SUPPORTED_INGRESS_CLASS\":\"Obsługiwana klasa Ingress\",\"CONTROLLER_CHART_NAME\":\"Nazwa schematu sterownika\",\"CONTROLLER_CHART_ARCHIEVE\":\"Archiwum wykresów sterownika\",\"RESOURCE_CONFIG_OPTION\":\"Opcja konfiguracji zasobów\",\"EXTERNAL_SERVICE_DOMAIN\":\"Zewnętrzna domena usługi\",\"TLS_SUPPORTED\":\"Obsługa TLS\",\"DEPLOYMENT\":\"Deployment\",\"NAMESPACE_CONFIG_OPTION\":\"Opcja konfiguracji przestrzeni nazw\",\"DEFAULT_NAMESPACE\":\"Domyślna przestrzeń nazw\",\"DEFAULT_STORAGE_CLASS\":\"Domyślna klasa pamięci\",\"USER_LOCAL_GITLAB_INSTANCE\":\"Użyj lokalnej instancji GitLab\",\"ATTACH_POINT\":\"Dołącz punkt\",\"ROUTER_NAME\":\"Nazwa routera\",\"ROUTER_ID\":\"ID routera\",\"ROUTER_INTERFACE_NAME\":\"Nazwa interfejsu routera\",\"EXTERNAL_NETWORKS\":\"Sieci zewnętrzne\",\"ADDRESS\":\"Adres\",\"NETWORK\":\"Sieć\",\"NETMASK_LENGTH\":\"Długość maski sieci\",\"ASSIGNED\":\"Assigned\",\"ASSIGNED_SINCE\":\"Assigned since\",\"ASSIGNED_TO\":\"Assigned to\",\"ADD_BUTTON\":\"Dodaj\",\"VIEW_BUTTON\":\"View\",\"EDIT_BUTTON\":\"Edit\",\"SUBMIT_BUTTON\":\"Prześlij Prześlij\",\"REMOVE_BUTTON\":\"Usunąć\",\"HOST_ADDRESS_REQUIRED_MESSAGE\":\"Wymagany jest host host address\",\"HOST_ADDRESS_VALIDATION_MESSAGE\":\"Adres hosta kontrolnego musi zawierać tylko cyfry i kropki\",\"HOST_SSH_USERNAME_REQUIRED_MESSAGE\":\"Wymagana jest nazwa hosta SSH\",\"HELM_CHART_REPO_NAME_REQUIRED_MESSAGE\":\"Nazwa repozytorium wykresu hełmu jest wymagana\",\"HELM_CHART_DIRECTORY_REQUIRED_MESSAGE\":\"Katalog map hełmu jest wymagany\",\"REST_API_HOST_ADDRESS_REQUIRED_MESSAGE\":\"Adres hosta API REST jest wymagany\",\"REST_API_HOST_ADDRESS_VALIDATION_MESSAGE\":\"Adres hosta API REST może zawierać tylko cyfry i kropki\",\"REST_API_PORT_REQUIRED_MESSAGE\":\"Wymagany jest port hosta REST API\",\"IP_REQUIRED_MESSAGE\":\"IP jest wymagane\",\"IP_VALIDATION_MESSAGE\":\"IP musi zawierać tylko cyfry i kropki\",\"NETWORK_REQUIRED_MESSAGE\":\"Wymagana jest sieć\",\"NETWORK_ADDRESS_VALIDATION_MESSAGE\":\"Adres sieciowy może zawierać tylko cyfry i kropki\",\"MASK_VALIDATION_MESSAGE\":\"Maska musi zawierać się między 0 a 32\"},\"GIT_LAB\":{\"TITLE\":\"Konfiguracja GitLab\",\"SERVER_ADDRESS\":\"Adres serwera\",\"SERVER_ADDRESS_REQUIRED_MESSAGE\":\"Adres serwera jest wymagany\",\"PORT\":\"Port\",\"PORT_REQUIRED_MESSAGE\":\"Port jest wymagany\",\"SSH_SERVER\":\"Serwer Ssh\",\"SSH_SERVER_REQUIRED_MESSAGE\":\"Serwer Ssh jest wymagany\",\"TOKEN\":\"Token\",\"TOKEN_REQUIRED_MESSAGE\":\"Token jest wymagany\",\"REPO_ACCESS_USERNAME\":\"Nazwa użytkownika dostępu do repozytorium\",\"REPO_ACCESS_USERNAME_REQUIRED_MESSAGE\":\"Wymagana jest nazwa użytkownika z dostępem do repozytorium\",\"VIEW_BUTTON\":\"Widok\",\"SUBMIT_BUTTON\":\"Submit\",\"EDIT_BUTTON\":\"Edytuj\"},\"SAML_PROXY\":{\"TITLE\":\"Konfiguracja SAML Proxy\",\"LOGIN_URL\":\"URL logowania\",\"LOGOUT_URL\":\"Logout URL\",\"SSO_KEY_FILE_PATH\":\"Ścieżka do pliku klucza SSO\",\"TIMEOUT\":\"Timeout [minuty]\",\"VIEW_BUTTON\":\"Widok\",\"SUBMIT_BUTTON\":\"Submit\",\"EDIT_BUTTON\":\"Edytuj\",\"LOGIN_URL_REQUIRED_MESSAGE\":\"Wymagany jest login URL\",\"LOGOUT_URL_REQUIRED_MESSAGE\":\"Wymagany jest wylogowany URL\",\"SSO_KEY_REQUIRED_MESSAGE\":\"Wymagany jest klucz SSO\",\"TIMEOUT_REQUIRED_MESSAGE\":\"wymagany jest limit czasu\"},\"USERS\":{\"TITLE\":\"Użytkownicy\",\"USER_NAME\":\"Nazwa użytkownika\",\"DOMAINS\":\"Domains\",\"GLOBAL_ROLE\":\"Globalna rola\",\"ROLES\":\"Role\",\"ENABLED\":\"Enabled\",\"ENABLED_BUTTON\":\"Włączone\",\"DETAILS_BUTTON\":\"Szczegóły\",\"DISABLE_BUTTON\":\"Wyłącz\",\"REMOVE_FROM_DOMAIN_BUTTON\":\"Usuń z domeny\"},\"DOMAINS\":{\"TITLE\":\"Domains\",\"CODE_NAME\":\"Codename\",\"NAME\":\"Nazwa\",\"ACTIVATE\":\"Aktywny\",\"DETAILS_BUTTON\":\"Szczegóły\",\"EDIT_BUTTON\":\"Edytuj\",\"REMOVE_BUTTON\":\"Usuń\"},\"APP_INSTANCES\":{\"TITLE\":\"Instancje aplikacji\",\"SHOW\":\"Pokaż\",\"NAME\":\"Nazwa\",\"DOMAIN\":\"Domena\",\"OWNER\":\"Właściciel\",\"DEPLOYED_AT\":\"Deployed at\",\"APPLICATION\":\"Aplikacja\",\"STATE\":\"Stan\",\"DETAILS_BUTTON\":\"Szczegóły\",\"GO_TO_APP_BUTTON\":\"Przejdź do aplikacji\"}}";
        }
        return json;
    }
}