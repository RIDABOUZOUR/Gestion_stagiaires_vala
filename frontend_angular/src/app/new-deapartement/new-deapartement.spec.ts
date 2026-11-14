import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewDeapartement } from './new-deapartement';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('NewDeapartement', () => {
  let component: NewDeapartement;
  let fixture: ComponentFixture<NewDeapartement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [NewDeapartement],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(NewDeapartement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
