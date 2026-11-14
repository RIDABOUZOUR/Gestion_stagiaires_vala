// Environnement de production.
// L'URL est remplacée au build Docker par la valeur de l'argument API_URL
// (voir frontend_angular/Dockerfile). En build local, la valeur par défaut s'applique.
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080'
};
