import { Role } from './auth.model';

export interface Utilisateur {
  id: number;
  email: string;
  role: Role;
  encadrantId: number | null;
  encadrantNom: string | null;
  actif: boolean;
}

export interface UtilisateurRequest {
  email: string;
  motDePasse: string;
  role: Role;
  encadrantId: number | null;
}

export interface UtilisateurModification {
  role: Role;
  encadrantId: number | null;
  actif: boolean;
}
