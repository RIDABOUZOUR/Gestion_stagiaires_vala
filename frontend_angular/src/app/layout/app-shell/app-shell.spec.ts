import { Router } from '@angular/router';
import { Role } from '../../models/auth.model';
import { AuthService } from '../../services/auth';
import { AppShell } from './app-shell';

describe('AppShell : menu selon le rôle', () => {
  const menuPour = (role: Role): string[] => {
    const auth = { aRole: (...roles: Role[]) => roles.includes(role) } as unknown as AuthService;
    return new AppShell({} as Router, auth).navVisibles.map((item) => item.route);
  };

  it("l'ADMIN voit tout, y compris les comptes utilisateurs", () => {
    expect(menuPour('ADMIN')).toContain('/utilisateurs');
    expect(menuPour('ADMIN')).toContain('/dashboard');
  });

  it('le RH voit la gestion mais pas les comptes', () => {
    expect(menuPour('RH')).toContain('/candidatures');
    expect(menuPour('RH')).not.toContain('/utilisateurs');
  });

  it("l'ENCADRANT ne voit ni tableau de bord, ni candidatures, ni comptes", () => {
    expect(menuPour('ENCADRANT')).toEqual(['/stagiaires', '/stages', '/encadrants', '/departements']);
  });
});
