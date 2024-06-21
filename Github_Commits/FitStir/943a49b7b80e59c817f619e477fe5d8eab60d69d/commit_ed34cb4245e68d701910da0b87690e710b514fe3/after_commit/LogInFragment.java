package com.fitstir.fitstirapp.ui.connect;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.fitstir.fitstirapp.ConnectActivity;
import com.fitstir.fitstirapp.MainActivity;
import com.fitstir.fitstirapp.R;
import com.fitstir.fitstirapp.databinding.FragmentLogInBinding;
import com.fitstir.fitstirapp.ui.utility.Methods;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.Objects;
import java.util.TimerTask;

public class LogInFragment extends Fragment {

    private FragmentLogInBinding binding;
    private ImageButton ForgotButton, visible, invisible;
    private FirebaseAuth auth;
    private EditText user_Email, user_Password;
    private GoogleSignInClient client;
    private GoogleSignInOptions options;
    private SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConnectViewModel connectViewModel =
                new ViewModelProvider(this).get(ConnectViewModel.class);

        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).hide();


        // Addition Text Here
        auth = FirebaseAuth.getInstance();
        user_Email = root.findViewById(R.id.username_log_in);
        user_Password = root.findViewById(R.id.user_password);
        sharedPreferences = getActivity().getSharedPreferences("user_Login", Context.MODE_PRIVATE);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleResult(task);
                }
            }
        });

        //google one tap sign in setup
        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this.requireContext(), options);
        Button google = root.findViewById(R.id.button_google_signIn);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = client.getSignInIntent();
                launcher.launch(intent);
            }
        });



        Button logInButton = root.findViewById(R.id.button_log_in);
        logInButton.setOnClickListener(v -> {

            //firebase authentication method
            validate();
        });

        ForgotButton = root.findViewById(R.id.button_forgot_pass);
        ForgotButton.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(R.id.action_navigation_log_in_to_navigation_forgot);
        });

        visible = root.findViewById(R.id.view_password);
        visible.setOnClickListener(v-> {

            if(visible.getVisibility() == View.VISIBLE)
            {
                visible.setVisibility(View.INVISIBLE);
                invisible.setVisibility(View.VISIBLE);
                user_Password.setInputType(View.VISIBLE);

            }
        });

        invisible = root.findViewById(R.id.invisible_password);
        invisible.setOnClickListener(v-> {
            if(invisible.getVisibility() == View.VISIBLE)
            {
                invisible.setVisibility(View.INVISIBLE);
                visible.setVisibility(View.VISIBLE);
                user_Password.setTransformationMethod(new PasswordTransformationMethod());
            }
        });
        //End
        return root;
    }

    private void handleResult(Task<GoogleSignInAccount> task) {
        task.addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
            @Override
            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                Toast.makeText(requireActivity(), googleSignInAccount.getDisplayName(), Toast.LENGTH_SHORT).show();
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                Objects.requireNonNull(getActivity()).startActivity(myIntent);

            }
        }). addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireActivity(), e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireActivity());
        if(account != null){
            Toast.makeText(requireActivity(),account.getDisplayName(), Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(getActivity(), MainActivity.class);
            Objects.requireNonNull(getActivity()).startActivity(myIntent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void validate(){
    String email = user_Email.getText().toString();
    String password = user_Password.getText().toString();

    final Drawable warning = AppCompatResources.getDrawable(requireContext(),R.drawable.baseline_warning_amber_24);
    warning.setBounds(0,0, warning.getIntrinsicWidth(),warning.getIntrinsicHeight());


    user_Password.setTransformationMethod(new PasswordTransformationMethod());
    if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
        if(!password.isEmpty() &&  password.length() >= 6){
            auth.signInWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>(){
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(getActivity(), "LogIn Successfull", Toast.LENGTH_SHORT).show();
                            Intent myIntent = new Intent(getActivity(), MainActivity.class);
                            Objects.requireNonNull(getActivity()).startActivity(myIntent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "LogIn Failed...Please Try Again!!!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else if(password.isEmpty())
        {
            user_Password.setError("Password can not be empty",warning);
        }
        else
        {
            user_Password.setError("Password must be at least 6 characters", warning);
        }

    }
    else if(email.isEmpty())
    {
        user_Email.setError("Email can not be empty...Please Try Again!!", warning);
    }
    else
    {
        user_Email.setError("Please enter valid Email!!", warning);
    }
    }
}