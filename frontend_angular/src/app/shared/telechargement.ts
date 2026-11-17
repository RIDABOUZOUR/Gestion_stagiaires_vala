import { HttpResponse } from '@angular/common/http';

/**
 * Enregistre un fichier reçu de l'API (PDF, CSV). Le téléchargement passe par
 * HttpClient — et non par un simple lien — pour que le jeton soit envoyé.
 */
export function enregistrerFichier(reponse: HttpResponse<Blob>, nomParDefaut: string): void {
  if (!reponse.body || typeof document === 'undefined') {
    return;
  }
  const url = URL.createObjectURL(reponse.body);
  const lien = document.createElement('a');
  lien.href = url;
  lien.download = nomDuFichier(reponse.headers.get('Content-Disposition')) ?? nomParDefaut;
  document.body.appendChild(lien);
  lien.click();
  lien.remove();
  setTimeout(() => URL.revokeObjectURL(url), 0);
}

/** Extrait le nom de fichier d'un en-tête Content-Disposition. */
export function nomDuFichier(contentDisposition: string | null): string | null {
  if (!contentDisposition) {
    return null;
  }
  const encode = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition);
  if (encode) {
    return decodeURIComponent(encode[1]);
  }
  const simple = /filename="?([^";]+)"?/i.exec(contentDisposition);
  return simple ? simple[1] : null;
}

/** Message lisible d'une erreur HTTP dont le corps est un Blob (réponse JSON d'erreur). */
export async function messageErreurBlob(erreur: unknown, parDefaut: string): Promise<string> {
  const corps = (erreur as { error?: unknown })?.error;
  if (corps instanceof Blob) {
    try {
      const json = JSON.parse(await corps.text()) as { message?: string };
      return json.message ?? parDefaut;
    } catch {
      return parDefaut;
    }
  }
  return parDefaut;
}

/** Paramètres de requête sans les valeurs vides. */
export function parametres(valeurs: Record<string, string | number | null | undefined>): Record<string, string> {
  const resultat: Record<string, string> = {};
  for (const [cle, valeur] of Object.entries(valeurs)) {
    if (valeur !== null && valeur !== undefined && String(valeur).trim() !== '') {
      resultat[cle] = String(valeur).trim();
    }
  }
  return resultat;
}
