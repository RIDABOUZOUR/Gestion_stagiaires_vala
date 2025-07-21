import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Navbar } from './navbar/navbar';
import { Stagiaires } from './stagiaires/stagiaires';
import { Stages } from './stages/stages';

import { Encadrants } from './encadrants/encadrants';
import { Departements } from './departements/departements';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Dashboard } from './dashboard/dashboard';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NewStage } from './new-stage/new-stage';
import { NewDeapartement } from './new-deapartement/new-deapartement';
import { NewEncadrant } from './new-encadrant/new-encadrant';
import { NewStagiaire } from './new-stagiaire/new-stagiaire';
import { EditDepartement } from './edit-departement/edit-departement';
import { EditEncadrant } from './edit-encadrant/edit-encadrant';
import { EditStage } from './edit-stage/edit-stage';
import { EditStagiaire } from './edit-stagiaire/edit-stagiaire';

@NgModule({
  declarations: [
    App,
    Navbar,
    Stagiaires,
    Stages,
    Encadrants,
    Departements,
    Dashboard,
    NewStage,
    NewDeapartement,
    NewEncadrant,
    NewStagiaire,
    EditDepartement,
    EditEncadrant,
    EditStage,
    EditStagiaire
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideClientHydration(withEventReplay())
  ],
  bootstrap: [App]
})
export class AppModule { }
