import { StageStatus } from './stage.model';

export type OrigineChangement = 'MANUEL' | 'AUTOMATIQUE';

export interface StageHistorique {
  ancienStatut: StageStatus | null;
  nouveauStatut: StageStatus;
  dateChangement: string;
  origine: OrigineChangement;
  auteur: string | null;
}

export type StatutPresence = 'PRESENT' | 'RETARD' | 'ABSENT' | 'ABSENCE_JUSTIFIEE';

export interface Presence {
  id: number;
  stagiaireId: number;
  date: string;
  statut: StatutPresence;
  commentaire: string | null;
}

export type StatutRapport = 'SOUMIS' | 'A_REVOIR' | 'VALIDE';

export interface Rapport {
  id: number;
  stagiaireId: number;
  semaine: string;
  travauxRealises: string;
  difficultes: string | null;
  objectifs: string | null;
  dateDepot: string;
  statut: StatutRapport;
  commentaireEncadrant: string | null;
}

export interface RapportRequest {
  semaine: string;
  travauxRealises: string;
  difficultes: string | null;
  objectifs: string | null;
}

export interface Evaluation {
  id: number;
  stagiaireId: number;
  noteGlobale: number;
  competenceTechnique: number;
  communication: number;
  autonomie: number;
  assiduite: number;
  appreciation: string | null;
  recommande: boolean;
  dateEvaluation: string;
}

export type EvaluationRequest = Omit<Evaluation, 'id' | 'stagiaireId' | 'dateEvaluation'>;
