import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription, filter } from 'rxjs';

interface NavItem {
  label: string;
  route: string;
  icon: string;
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
    { label: 'Tableau de bord', route: '/dashboard', icon: 'squares-2x2' },
    { label: 'Stagiaires', route: '/stagiaires', icon: 'users' },
    { label: 'Stages', route: '/stages', icon: 'briefcase' },
    { label: 'Encadrants', route: '/encadrants', icon: 'identification' },
    { label: 'Départements', route: '/departements', icon: 'building-office' },
  ];

  private readonly routerSub = new Subscription();

  constructor(private readonly router: Router) {}

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
