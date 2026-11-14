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

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: Dashboard, data: { title: 'Tableau de bord' } },
  { path: 'stagiaires', component: Stagiaires, data: { title: 'Stagiaires' } },
  { path: 'stages', component: Stages, data: { title: 'Stages' } },
  { path: 'encadrants', component: Encadrants, data: { title: 'Encadrants' } },
  { path: 'departements', component: Departements, data: { title: 'Départements' } },
  { path: 'new-stage', component: NewStage, data: { title: 'Nouveau stage' } },
  { path: 'new-departement', component: NewDeapartement, data: { title: 'Nouveau département' } },
  { path: 'new-encadrant', component: NewEncadrant, data: { title: 'Nouvel encadrant' } },
  { path: 'new-stagiaire', component: NewStagiaire, data: { title: 'Nouveau stagiaire' } },
  { path: 'edit-departement/:id', component: EditDepartement, data: { title: 'Modifier un département' } },
  { path: 'edit-encadrant/:id', component: EditEncadrant, data: { title: 'Modifier un encadrant' } },
  { path: 'edit-stage/:id', component: EditStage, data: { title: 'Modifier un stage' } },
  { path: 'edit-stagiaire/:id', component: EditStagiaire, data: { title: 'Modifier un stagiaire' } },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
