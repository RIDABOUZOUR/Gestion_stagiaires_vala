import { TestBed } from '@angular/core/testing';

import { Stagiaire } from './stagiaire';

describe('Stagiaire', () => {
  let service: Stagiaire;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Stagiaire);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
