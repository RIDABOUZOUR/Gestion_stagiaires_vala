import { Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { ConfirmService } from './confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: false,
  template: `
    <div
      *ngIf="confirm.state$ | async as state"
      class="fixed inset-0 z-[70] flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-titre"
      aria-describedby="confirm-message">
      <div
        class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm"
        (click)="confirm.respond(false)"
        aria-hidden="true"></div>
      <div class="relative w-full max-w-md rounded-2xl bg-white p-6 shadow-xl ring-1 ring-slate-200">
        <h2 id="confirm-titre" class="text-lg font-semibold text-slate-900">{{ state.title || 'Confirmation' }}</h2>
        <p id="confirm-message" class="mt-2 text-sm text-slate-600">{{ state.message }}</p>
        <div class="mt-6 flex justify-end gap-3">
          <button #annuler type="button" class="btn btn-secondary" (click)="confirm.respond(false)">
            {{ state.cancelLabel || 'Annuler' }}
          </button>
          <button
            type="button"
            class="btn"
            [class.btn-danger]="state.danger"
            [class.btn-primary]="!state.danger"
            (click)="confirm.respond(true)">
            {{ state.confirmLabel || 'Confirmer' }}
          </button>
        </div>
      </div>
    </div>
  `,
})
export class ConfirmDialogComponent {
  constructor(public readonly confirm: ConfirmService) {}

  /** Focus sur « Annuler » à l'ouverture : choix sûr par défaut, et clavier utilisable tout de suite. */
  @ViewChild('annuler')
  set boutonAnnuler(bouton: ElementRef<HTMLButtonElement> | undefined) {
    bouton?.nativeElement.focus();
  }

  @HostListener('document:keydown.escape')
  fermer(): void {
    this.confirm.respond(false);
  }
}
