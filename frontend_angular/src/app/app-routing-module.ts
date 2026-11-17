import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Dashboard } from './dashboard/dashboard';
import { Stagiaires } from './stagiaires/stagiaires';
import { Stages } from './stages/stages';
import { Encadrants } from './encadrants/encadrants';
import { Departements } from './departements/departements';
import { NewStage } from './new-stage/new-stage';
import { NewDeapartement } from './new-deapartement/new-deapartement';
import { NewEncadrant } from './new-encadrant/new-encadrant';
import { NewStagiaire } from './new-stagiaire/new-stagiaire';
import { EditDepartement } from './edit-departement/edit-departement';
import { EditEncadrant } from './edit-encadrant/edit-encadrant';
import { EditStage } from './edit-stage/edit-stage';
import { EditStagiaire } from './edit-stagiaire/edit-stagiaire';
import { Login } from './login/login';
import { StageDetail } from './stage-detail/stage-detail';
import { StagiaireDetail } from './stagiaire-detail/stagiaire-detail';
import { Candidatures } from './candidatures/candidatures';
import { CandidatureDetail } from './candidature-detail/candidature-detail';
import { Postuler } from './postuler/postuler';
import { Utilisateurs } from './utilisateurs/utilisateurs';
import { Role } from './models/auth.model';
import { accueilGuard, authGuard, invitesGuard } from './shared/auth/auth.guards';

/** Création, modification et tableau de bord : réservés à la gestion (ADMIN, RH). */
const GESTION: Role[] = ['ADMIN', 'RH'];

const routes: Routes = [
  { path: 'login', component: Login, canActivate: [invitesGuard], data: { title: 'Connexion' } },
  { path: '', pathMatch: 'full', canActivate: [accueilGuard], children: [] },
  // Dépôt de candidature : public (dans le menu pour la gestion, plein écran pour un visiteur).
  { path: 'postuler', component: Postuler, data: { title: 'Nouvelle candidature' } },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: Dashboard, canActivate: [authGuard], data: { title: 'Tableau de bord', roles: GESTION } },
      { path: 'stagiaires', component: Stagiaires, data: { title: 'Stagiaires' } },
      { path: 'stagiaires/:id', component: StagiaireDetail, data: { title: 'Fiche stagiaire' } },
      { path: 'stages', component: Stages, data: { title: 'Stages' } },
      { path: 'stages/:id', component: StageDetail, data: { title: 'Fiche stage' } },
      { path: 'candidatures', component: Candidatures, canActivate: [authGuard], data: { title: 'Candidatures', roles: GESTION } },
      { path: 'candidatures/:id', component: CandidatureDetail, canActivate: [authGuard], data: { title: 'Candidature', roles: GESTION } },
      { path: 'utilisateurs', component: Utilisateurs, canActivate: [authGuard], data: { title: 'Comptes utilisateurs', roles: ['ADMIN'] } },
      { path: 'encadrants', component: Encadrants, data: { title: 'Encadrants' } },
      { path: 'departements', component: Departements, data: { title: 'Départements' } },
      { path: 'new-stage', component: NewStage, canActivate: [authGuard], data: { title: 'Nouveau stage', roles: GESTION } },
      { path: 'new-departement', component: NewDeapartement, canActivate: [authGuard], data: { title: 'Nouveau département', roles: GESTION } },
      { path: 'new-encadrant', component: NewEncadrant, canActivate: [authGuard], data: { title: 'Nouvel encadrant', roles: GESTION } },
      { path: 'new-stagiaire', component: NewStagiaire, canActivate: [authGuard], data: { title: 'Nouveau stagiaire', roles: GESTION } },
      { path: 'edit-departement/:id', component: EditDepartement, canActivate: [authGuard], data: { title: 'Modifier un département', roles: GESTION } },
      { path: 'edit-encadrant/:id', component: EditEncadrant, canActivate: [authGuard], data: { title: 'Modifier un encadrant', roles: GESTION } },
      { path: 'edit-stage/:id', component: EditStage, canActivate: [authGuard], data: { title: 'Modifier un stage', roles: GESTION } },
      { path: 'edit-stagiaire/:id', component: EditStagiaire, canActivate: [authGuard], data: { title: 'Modifier un stagiaire', roles: GESTION } },
    ],
  },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
