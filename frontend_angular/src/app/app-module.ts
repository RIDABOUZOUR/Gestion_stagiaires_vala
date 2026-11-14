import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import {
  BrowserModule,
  provideClientHydration,
  withEventReplay,
} from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { AppShell } from './layout/app-shell/app-shell';
import { IconComponent } from './shared/icon/icon.component';
import { ToastContainerComponent } from './shared/notification/toast-container.component';
import { ConfirmDialogComponent } from './shared/confirm/confirm-dialog.component';
import { Stagiaires } from './stagiaires/stagiaires';
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

@NgModule({
  declarations: [
    App,
    AppShell,
    ToastContainerComponent,
    ConfirmDialogComponent,
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
    EditStagiaire,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    IconComponent,
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideClientHydration(withEventReplay()),
    provideHttpClient(),
  ],
  bootstrap: [App],
})
export class AppModule {}
