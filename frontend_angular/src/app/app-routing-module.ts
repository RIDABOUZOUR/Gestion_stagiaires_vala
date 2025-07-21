import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Stagiaires } from './stagiaires/stagiaires';
import { Navbar } from './navbar/navbar';
import { Stages } from './stages/stages';
import { Encadrants } from './encadrants/encadrants';
import { Departements } from './departements/departements';

import { Dashboard } from './dashboard/dashboard';
import { NewStage } from './new-stage/new-stage';
import { NewDeapartement } from './new-deapartement/new-deapartement';
import { NewEncadrant } from './new-encadrant/new-encadrant';
import { NewStagiaire } from './new-stagiaire/new-stagiaire';
import { EditDepartement } from './edit-departement/edit-departement';
import { EditEncadrant } from './edit-encadrant/edit-encadrant';
import { EditStage } from './edit-stage/edit-stage';
import { EditStagiaire } from './edit-stagiaire/edit-stagiaire';

const routes: Routes = [
   {path : "", component:Dashboard},
  {path : "stagiaires", component:Stagiaires},
  {path : "stages", component:Stages},
  {path : "encadrants", component:Encadrants},
  {path : "departements", component:Departements},
  {path : "dashboard", component:Dashboard},
  {path : "new-stage", component:NewStage},
  {path : "new-departement", component:NewDeapartement},
  {path : "new-encadrant",component:NewEncadrant},
  {path : "new-stagiaire",component:NewStagiaire},
  { path: 'edit-departement/:id', component: EditDepartement },
  { path: 'edit-encadrant/:id', component: EditEncadrant },
  { path: 'edit-stage/:id', component: EditStage },
  {path: 'edit-stagiaire/:id', component: EditStagiaire}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
