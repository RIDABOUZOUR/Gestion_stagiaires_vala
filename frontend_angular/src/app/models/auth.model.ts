export type Role = 'ADMIN' | 'RH' | 'ENCADRANT';

export const LIBELLES_ROLE: Record<Role, string> = {
  ADMIN: 'Administrateur',
  RH: 'Ressources humaines',
  ENCADRANT: 'Encadrant',
};

/** Réponse de POST /auth/login. */
export interface LoginResponse {
  token: string;
  expiresAt: string;
  email: string;
  role: Role;
  encadrantId: number | null;
}

/** Session ouverte côté navigateur. */
export interface Session {
  token: string;
  /** Expiration du jeton, en millisecondes depuis l'époque. */
  expiresAt: number;
  email: string;
  role: Role;
  encadrantId: number | null;
}
