import { Component } from '@angular/core';
import { NotificationService } from './notification.service';

@Component({
  selector: 'app-toast-container',
  standalone: false,
  template: `
    <div
      class="pointer-events-none fixed inset-x-0 top-4 z-[60] flex flex-col items-center gap-2 px-4"
      aria-live="polite"
      aria-atomic="true">
      <div
        *ngFor="let toast of notifications.toasts$ | async"
        class="pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl bg-white p-4 shadow-lg ring-1 ring-slate-200">
        <app-icon
          [name]="toast.type === 'success' ? 'check-circle' : toast.type === 'error' ? 'x-mark' : 'identification'"
          [size]="toast.type === 'error' ? 'h-5 w-5 shrink-0 text-rose-500' : 'h-5 w-5 shrink-0 text-emerald-500'"></app-icon>
        <p class="flex-1 text-sm text-slate-700">{{ toast.text }}</p>
        <button
          type="button"
          class="btn-icon -m-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
          (click)="notifications.dismiss(toast.id)"
          aria-label="Fermer">
          <app-icon name="x-mark" size="h-4 w-4"></app-icon>
        </button>
      </div>
    </div>
  `,
})
export class ToastContainerComponent {
  constructor(public readonly notifications: NotificationService) {}
}
