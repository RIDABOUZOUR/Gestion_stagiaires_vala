/** Dates ISO « aaaa-mm-jj » manipulées sans fuseau horaire (pas de décalage d'un jour). */

const pad = (n: number) => String(n).padStart(2, '0');

export function isoDate(annee: number, mois: number, jour: number): string {
  return `${annee}-${pad(mois)}-${pad(jour)}`;
}

export function aujourdhui(): string {
  const d = new Date();
  return isoDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
}

/** « aaaa-mm » du mois d'une date ISO. */
export function moisDe(date: string): string {
  return date.slice(0, 7);
}

export function decalerMois(mois: string, delta: number): string {
  const [a, m] = mois.split('-').map(Number);
  const d = new Date(a, m - 1 + delta, 1);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}`;
}

export function joursDansLeMois(mois: string): number {
  const [a, m] = mois.split('-').map(Number);
  return new Date(a, m, 0).getDate();
}

/** Jour de la semaine, lundi = 0 … dimanche = 6. */
export function jourSemaine(date: string): number {
  const [a, m, j] = date.split('-').map(Number);
  return (new Date(a, m - 1, j).getDay() + 6) % 7;
}

/** Lundi de la semaine d'une date ISO. */
export function lundiDe(date: string): string {
  const [a, m, j] = date.split('-').map(Number);
  const d = new Date(a, m - 1, j - jourSemaine(date));
  return isoDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
}

export function ajouterJours(date: string, jours: number): string {
  const [a, m, j] = date.split('-').map(Number);
  const d = new Date(a, m - 1, j + jours);
  return isoDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
}

export function libelleMois(mois: string): string {
  const [a, m] = mois.split('-').map(Number);
  const texte = new Date(a, m - 1, 1).toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  return texte.charAt(0).toUpperCase() + texte.slice(1);
}
