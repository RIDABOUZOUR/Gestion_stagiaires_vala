import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Departement } from './departement';

describe('Departement', () => {
  let service: Departement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Departement);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
