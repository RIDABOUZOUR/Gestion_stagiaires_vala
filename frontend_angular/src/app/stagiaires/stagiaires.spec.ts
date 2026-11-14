import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Stagiaires } from './stagiaires';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('Stagiaires', () => {
  let component: Stagiaires;
  let fixture: ComponentFixture<Stagiaires>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [Stagiaires],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(Stagiaires);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
