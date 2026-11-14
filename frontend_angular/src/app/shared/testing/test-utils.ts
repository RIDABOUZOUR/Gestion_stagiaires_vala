import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, provideRouter } from '@angular/router';
import { IconComponent } from '../icon/icon.component';

/** Imports communs aux tests de composants (directives, formulaires, icônes). */
export const TEST_IMPORTS = [
  CommonModule,
  FormsModule,
  ReactiveFormsModule,
  RouterModule,
  IconComponent,
];

/** Providers communs : routeur de test + client HTTP simulé. */
export const TEST_PROVIDERS = [provideRouter([]), provideHttpClient(), provideHttpClientTesting()];
