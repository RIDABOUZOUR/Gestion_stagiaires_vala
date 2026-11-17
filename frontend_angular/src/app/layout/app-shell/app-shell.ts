import { Component, OnDestroy, OnInit } from '@angular/core';
import { LIBELLES_ROLE, Role } from '../../models/auth.model';
import { AuthService } from '../../services/auth';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription, filter } from 'rxjs';

interface NavItem {
  label: string;
  route: string;
  icon: string;
  /** Rôles autorisés ; absent = tous les utilisateurs connectés. */
  roles?: Role[];
}

@Component({
  selector: 'app-shell',
  standalone: false,
  templateUrl: './app-shell.html',
})
export class AppShell implements OnInit, OnDestroy {
  sidebarOpen = false;
  pageTitle = 'Tableau de bord';
  readonly year = new Date().getFullYear();

  readonly navItems: NavItem[] = [
    { label: 'Tableau de bord', route: '/dashboard', icon: 'squares-2x2', roles: ['ADMIN', 'RH'] },
    { label: 'Stagiaires', route: '/stagiaires', icon: 'users' },
    { label: 'Stages', route: '/stages', icon: 'briefcase' },
    { label: 'Encadrants', route: '/encadrants', icon: 'identification' },
    { label: 'Départements', route: '/departements', icon: 'building-office' },
    { label: 'Candidatures', route: '/candidatures', icon: 'user-plus', roles: ['ADMIN', 'RH'] },
    { label: 'Utilisateurs', route: '/utilisateurs', icon: 'user-group', roles: ['ADMIN'] },
  ];

  private readonly routerSub = new Subscription();

  constructor(
    private readonly router: Router,
    readonly auth: AuthService,
  ) {}

  get navVisibles(): NavItem[] {
    return this.navItems.filter((item) => !item.roles || this.auth.aRole(...item.roles));
  }

  get libelleRole(): string {
    const role = this.auth.role();
    return role ? LIBELLES_ROLE[role] : '';
  }

  seDeconnecter(): void {
    this.auth.deconnecter();
    void this.router.navigate(['/login']);
  }

  ngOnInit(): void {
    this.routerSub.add(
      this.router.events
        .pipe(filter((event) => event instanceof NavigationEnd))
        .subscribe(() => {
          this.updateTitle();
          this.sidebarOpen = false;
        }),
    );
    this.updateTitle();
  }

  ngOnDestroy(): void {
    this.routerSub.unsubscribe();
  }

  private updateTitle(): void {
    let route = this.router.routerState.root;
    while (route.firstChild) {
      route = route.firstChild;
    }
    const title = route.snapshot.data['title'] as string | undefined;
    this.pageTitle = title ?? 'Tableau de bord';
  }
}
