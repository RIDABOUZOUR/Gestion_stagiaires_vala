import { CV_TAILLE_MAX } from '../models/candidature.model';

/**
 * Contrôle côté navigateur d'un CV choisi (confort de saisie ; l'API refait la
 * vérification sur le contenu réel du fichier). Renvoie un message d'erreur ou null.
 */
export function erreurCv(fichier: File | null | undefined): string | null {
  if (!fichier) {
    return null;
  }
  const estPdf = fichier.type === 'application/pdf' || fichier.name.toLowerCase().endsWith('.pdf');
  if (!estPdf) {
    return 'Le CV doit être un fichier PDF.';
  }
  if (fichier.size === 0) {
    return 'Le CV est vide.';
  }
  if (fichier.size > CV_TAILLE_MAX) {
    return 'Le CV dépasse 5 Mo.';
  }
  return null;
}

/** Taille lisible : « 850 Ko », « 2,4 Mo ». */
export function tailleLisible(octets: number): string {
  if (octets < 1024 * 1024) {
    return `${Math.max(1, Math.round(octets / 1024))} Ko`;
  }
  return `${(octets / (1024 * 1024)).toFixed(1).replace('.', ',')} Mo`;
}
