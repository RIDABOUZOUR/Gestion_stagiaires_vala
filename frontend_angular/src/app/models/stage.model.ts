export interface EncadrantModel {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
}

export enum StageStatus {
  PLANIFIE = 'PLANIFIE',
  EN_COURS = 'EN_COURS',
  TERMINE = 'TERMINE',
  ANNULE = 'ANNULE',
}

export interface StageModel {
  id: number;
  sujet: string;
  dateDebut: string;
  dateFin: string;
  status: StageStatus;
  encadrant: EncadrantModel;
}

export interface DepartementModel {
  id: number;
  nom: string;
  responsable: string;
}

export interface StagiaireModel {
  id: number;
  nom: string;
  prenom: string;
  dateNaissance: string;
  sexe: Sexe;
  email: string;
  telephone: string;
  adresse: string;
  organisme: string;
  stage: StageModel;
  niveau: string;
  filiere: string;
  departement: DepartementModel;
}

export enum Sexe {
  HOMME = 'HOMME',
  FEMME = 'FEMME',
}
