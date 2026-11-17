import { Sexe } from './stage.model';

export type CandidatureStatus = 'SOUMISE' | 'EN_ETUDE' | 'ACCEPTEE' | 'REFUSEE' | 'RETIREE';

export interface Candidature {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  dateNaissance: string | null;
  sexe: Sexe | null;
  organisme: string | null;
  niveau: string | null;
  filiere: string | null;
  departementSouhaiteId: number | null;
  departementSouhaiteNom: string | null;
  dateDebutSouhaitee: string | null;
  dateFinSouhaitee: string | null;
  motivation: string | null;
  statut: CandidatureStatus;
  motifRefus: string | null;
  dateSoumission: string;
  dateDecision: string | null;
  stagiaireId: number | null;
  cvFourni: boolean;
}

/** Département proposé dans le formulaire public. */
export interface DepartementPublic {
  id: number;
  nom: string;
}

/** CV : PDF uniquement, 5 Mo au plus (mêmes règles que l'API). */
export const CV_TAILLE_MAX = 5 * 1024 * 1024;

export interface CandidatureRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  dateNaissance: string | null;
  sexe: Sexe;
  organisme: string | null;
  niveau: string | null;
  filiere: string | null;
  departementSouhaiteId: number | null;
  dateDebutSouhaitee: string | null;
  dateFinSouhaitee: string | null;
  motivation: string | null;
}

export interface CandidatureFiltre {
  keyword?: string;
  statut?: CandidatureStatus | '';
  departementId?: number | null;
}
