import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Stagiaires } from './stagiaires/stagiaires';
import { Navbar } from './navbar/navbar';
import { Stages } from './stages/stages';
import { Encadrants } from './encadrants/encadrants';
import { Departements } from './departements/departements';

import { Dashboard } from './dashboard/dashboard';
import { NewStage } from './new-stage/new-stage';

const routes: Routes = [
   {path : "", component:Navbar},
  {path : "stagiaires", component:Stagiaires},
  {path : "stages", component:Stages},
  {path : "encadrants", component:Encadrants},
  {path : "departements", component:Departements},
  {path : "dashboard", component:Dashboard},
  {path : "new-stage", component:NewStage}

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
