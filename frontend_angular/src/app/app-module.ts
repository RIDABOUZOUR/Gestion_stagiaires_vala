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
import { ReactiveFormsModule } from '@angular/forms';
import { NewStage } from './new-stage/new-stage';

@NgModule({
  declarations: [
    App,
    Navbar,
    Stagiaires,
    Stages,
    Encadrants,
    Departements,
    Dashboard,
    NewStage
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideClientHydration(withEventReplay())
  ],
  bootstrap: [App]
})
export class AppModule { }
