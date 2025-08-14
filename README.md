# PMT – Déploiement Docker 

## Prérequis
- Docker + Docker Compose
- Node 20 (pour dev front local)
- JDK 21 + Maven (pour dev back local)
- Variables d'environnement à ajouter dans un .env :
     - SPRING_MAIL_HOST=smtp.gmail.com
     - SPRING_MAIL_PORT=587
     - SPRING_MAIL_USERNAME=PMTIscodWilliam@gmail.com
     - SPRING_MAIL_PASSWORD=oujo nyvf mpgi jmqm

## Lancer en local (Docker)
Dans un bash depuis la racine du projet :
- Pour Build :
    - docker compose build
- Pour demarrer :
    - docker compose up -d
- Pour arreter :
    - docker compose down 
- Pour arreter en supprimant les données :
    - docker compose down -v   
    
# Liens utiles : 
# Front : http://localhost:4200
# Back  : http://localhost:8080
# DB    : localhost:5432 (pmt/pmt)
