import { StageStatus } from '../models/stage.model';
import { CandidatureStatus } from '../models/candidature.model';
import { StatutPresence, StatutRapport } from '../models/suivi.model';

/** Libellés et couleurs des statuts, partagés par tous les écrans. */

export const STAGE_STATUT: Record<StageStatus, { libelle: string; badge: string }> = {
  PLANIFIE: { libelle: 'Planifié', badge: 'badge-slate' },
  EN_COURS: { libelle: 'En cours', badge: 'badge-green' },
  TERMINE: { libelle: 'Terminé', badge: 'badge-amber' },
  ANNULE: { libelle: 'Annulé', badge: 'badge-rose' },
};

/** Transitions autorisées par l'API (voir StageStatus côté backend). */
export const STAGE_TRANSITIONS: Record<StageStatus, StageStatus[]> = {
  PLANIFIE: [StageStatus.EN_COURS, StageStatus.ANNULE],
  EN_COURS: [StageStatus.TERMINE, StageStatus.ANNULE],
  TERMINE: [],
  ANNULE: [],
};

/** Statut final (TERMINE, ANNULE) : le stage n'est plus modifiable. */
export function estStatutFinal(status: StageStatus | null | undefined): boolean {
  return !!status && STAGE_TRANSITIONS[status].length === 0;
}

export const CANDIDATURE_STATUT: Record<CandidatureStatus, { libelle: string; badge: string }> = {
  SOUMISE: { libelle: 'Soumise', badge: 'badge-slate' },
  EN_ETUDE: { libelle: "À l'étude", badge: 'badge-amber' },
  ACCEPTEE: { libelle: 'Acceptée', badge: 'badge-green' },
  REFUSEE: { libelle: 'Refusée', badge: 'badge-rose' },
  RETIREE: { libelle: 'Retirée', badge: 'badge-slate' },
};

export const PRESENCE_STATUT: Record<StatutPresence, { libelle: string; court: string; classes: string }> = {
  PRESENT: { libelle: 'Présent', court: 'P', classes: 'bg-emerald-100 text-emerald-800 ring-emerald-200' },
  RETARD: { libelle: 'Retard', court: 'R', classes: 'bg-amber-100 text-amber-800 ring-amber-200' },
  ABSENT: { libelle: 'Absent', court: 'A', classes: 'bg-rose-100 text-rose-800 ring-rose-200' },
  ABSENCE_JUSTIFIEE: { libelle: 'Absence justifiée', court: 'J', classes: 'bg-sky-100 text-sky-800 ring-sky-200' },
};

export const RAPPORT_STATUT: Record<StatutRapport, { libelle: string; badge: string }> = {
  SOUMIS: { libelle: 'Soumis', badge: 'badge-slate' },
  A_REVOIR: { libelle: 'À revoir', badge: 'badge-rose' },
  VALIDE: { libelle: 'Validé', badge: 'badge-green' },
};
