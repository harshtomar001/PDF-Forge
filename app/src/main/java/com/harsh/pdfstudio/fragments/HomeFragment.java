package com.harsh.pdfstudio.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.CardAdapter;
import com.harsh.pdfstudio.adapter.SlideAdapter;
import com.harsh.pdfstudio.controllers.RateAppSheetController;
import com.harsh.pdfstudio.model.CardItem;
import com.harsh.pdfstudio.model.RecentFileItem;
import com.harsh.pdfstudio.ui.CompressPDF;
import com.harsh.pdfstudio.ui.CreatePDF;
import com.harsh.pdfstudio.ui.EditPDF;
import com.harsh.pdfstudio.ui.FeedBack;
import com.harsh.pdfstudio.ui.ImgFromPDF;
import com.harsh.pdfstudio.ui.MergePdfActivity;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.ui.PrivacyPolicy;
import com.harsh.pdfstudio.ui.RotatePDF;
import com.harsh.pdfstudio.ui.UpgradeActivity;
import com.harsh.pdfstudio.utils.DeletePDF;
import com.harsh.pdfstudio.utils.GetFirstPage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class HomeFragment extends Fragment {

    ImageView no_history_img;
    TextView no_history_text;
    TextView title;
    ImageView menuBtn;



    private View rateApp;

    private  homeActionListener listener;

    public void setOnHomeActionListener(homeActionListener listener){

        this.listener =  listener;

    }

    ExecutorService executors= Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    Class<?> destinatedActivityForViewPager = CreatePDF.class;


    public HomeFragment(){
        super(R.layout.ui__controller);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedBundle){

        super.onViewCreated(view, savedBundle);

        menuBtn=view.findViewById(R.id.menuBtn);

        title=view.findViewById(R.id.toolbarTitle);

        DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawerLayout);

        View toolbarDrawerLayout =  requireActivity().findViewById(R.id.menu_container_toolbar);

        View toolbarHomePage  =  view.findViewById(R.id.toolbar_home_page);

        if (toolbarDrawerLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbarDrawerLayout,
                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(WindowInsetsCompat.Type.statusBars());

                        v.setPadding(
                                v.getPaddingLeft(),
                                (int) (systemBars.top),
                                v.getPaddingRight(),
                                v.getPaddingBottom()
                        );

                        return insets;
                    });
        }

        if (toolbarHomePage != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbarHomePage,
                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(WindowInsetsCompat.Type.statusBars());

                        v.setPadding(
                                v.getPaddingLeft(),
                                (int) (systemBars.top / 1.5f),
                                v.getPaddingRight(),
                                v.getPaddingBottom()
                        );

                        return insets;
                    });
        }



        ImageView searchBtn = view.findViewById(R.id.searchBtn);

        if (searchBtn != null) {

            searchBtn.setOnClickListener(v -> {

                listener.onSearchClicked();

            });

        }

        ImageView notificationBtn = view.findViewById(R.id.notificationBtn);

        if (notificationBtn != null) {

            notificationBtn.setOnClickListener(v -> {

                listener.onNotificationClicked();

            });

        }


        View profile  = view.findViewById(R.id.profileBtn);

        profile.setOnClickListener(v -> {

            if (isUserLoggedIn()) {

                listener.onProfileClicked();

            }
            else {

                listener.onLoginClicked();

            }

        });




        SpannableString text = new SpannableString("PDF Studio");

        text.setSpan(
                new ForegroundColorSpan(Color.WHITE),
                0, 3,                     // PDF
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        text.setSpan(
                new ForegroundColorSpan(
                        ContextCompat
                                .getColor(
                                        requireContext(),
                                        R.color.gold)
                ),
                4,
                10,                    // STUDIO
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        title.setText(text);


        no_history_text=view.findViewById(R.id.no_recent_fileText);
        no_history_img=view.findViewById(R.id.no_recentFile_Img);

        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        List<CardItem> cardImage=new ArrayList<>();

        cardImage.add(new CardItem(R.drawable.image_to_pdf,"Images to PDF","Convert Images to PDF", CreatePDF.class));
        cardImage.add( new CardItem(R.drawable.merge_pdf ,"Merge PDF","Combine multiple PDF files", MergePdfActivity.class));
        cardImage.add( new CardItem(R.drawable.compress_pdf,"Compress PDF","Reduce PDF file size", CompressPDF.class));
        cardImage.add( new CardItem(R.drawable.edit_pdf,"Edit PDF","Edit PDF Pages", EditPDF.class));
        cardImage.add( new CardItem(R.drawable.rotate_pdf,"Rotate PDF" ,"Rotate PDF Pages", RotatePDF.class));
        cardImage.add( new CardItem(R.drawable.pdf_to_image,"PDF to Images","Convert PDF  to Images", ImgFromPDF.class));


        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_card);
        CardAdapter cardAdapter=new CardAdapter(cardImage);
        recyclerView.setClipToPadding(false);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(),3));

        recyclerView.setAdapter(cardAdapter);


        TextView createNow = view.findViewById(R.id.createNowBtnViewPager);


        List<Integer> images = Arrays.asList(
                R.drawable.create_pdf_,
                R.drawable.merge_pdf_,
                R.drawable.rotate_pdf_,
                R.drawable.compress_pdf_1
        );

        SlideAdapter adapter = new SlideAdapter(images, item -> {});

        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {

                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        switch (position) {

                            case 0:
                                createNow.setText("Create Now  ›");
                                destinatedActivityForViewPager = CreatePDF.class;
                                break;

                            case 1:
                                createNow.setText("Merge Now  ›");
                                destinatedActivityForViewPager = MergePdfActivity.class;
                                break;

                            case 2:
                                createNow.setText("Rotate Now  ›");
                                destinatedActivityForViewPager = RotatePDF.class;
                                break;

                            case 3:
                                createNow.setText("Compress Now  ›");
                                destinatedActivityForViewPager = CompressPDF.class;
                                break;
                        }
                    }
                }
        );

        int pageMarginPx = 30;

        viewPager.setPageTransformer((page, position) -> {

            page.setTranslationX(pageMarginPx * position);

        });

        TabLayout tabLayout = view.findViewById(R.id.tabDots);



        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    View dot = LayoutInflater.from(requireContext())
                            .inflate(R.layout.dot_layout, null);
                    tab.setCustomView(dot);
                }
        ).attach();


        createNow.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext() ,
                    destinatedActivityForViewPager
            );

            startActivity(intent);

        });



        TextView versionText=view.findViewById(R.id.versionText);

        loadRecentFiles(view);

        try{

            String versionName = requireContext()
                    .getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0)
                    .versionName;

            versionText.setText("Version "+versionName);


        }

        catch (Exception e){
            e.printStackTrace();
        }


        LinearLayout help = requireActivity().findViewById(R.id.item_help);
        LinearLayout terms = requireActivity().findViewById(R.id.item_terms);
        LinearLayout privacyPolicy = requireActivity().findViewById(R.id.item_privacy);
        LinearLayout aboutPdfStudio = requireActivity().findViewById(R.id.item_about);
        LinearLayout aboutDeveloper = requireActivity().findViewById(R.id.item_developer);
        LinearLayout feedback = requireActivity().findViewById(R.id.item_feedback);
        TextView userName = requireActivity().findViewById(R.id.tv_user_name);
        TextView userEmail = requireActivity().findViewById(R.id.tv_user_email);

        if (isUserLoggedIn()) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null){

                userName.setText(
                        user.getDisplayName() != null ?
                                user.getDisplayName() :
                                "Guest"
                );

                userEmail.setText(
                        user.getEmail() != null ?
                                user.getEmail() :
                                "guest@gmail.com"
                );


            }


        }


        menuBtn.setOnClickListener(v -> {

           drawerLayout.openDrawer(GravityCompat.START);;

        });



        LinearLayout unlockPremiumBtn = view.findViewById(R.id.unlockPremiumBtn);

        if (unlockPremiumBtn != null) {

            unlockPremiumBtn.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), UpgradeActivity.class);
                startActivity(intent);
            });

        }


        TextView seeAllTools = view.findViewById(R.id.seeAllTools);

        if (seeAllTools != null) {

            seeAllTools.setOnClickListener(v -> {
                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_tools);
                }

            });
        }

        // "See All" on Recent Files -> jump to the History tab in the bottom nav
        TextView seeAllRecent = view.findViewById(R.id.seeAllRecent);

        if (seeAllRecent != null) {

            seeAllRecent.setOnClickListener(v -> {
                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_history);
                }
            });

        }

        help.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(
                    Uri.parse(
                            "https://pdfstudioapp.github.io/pdfstudio.github.io/help.html"
                    )
            );

            startActivity(intent);

        });

        terms.setOnClickListener(v -> {

            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setData(

                    Uri.parse(
                            "https://pdfstudioapp.github.io/pdfstudio.github.io/terms_services.html"
                    )
            );

            startActivity(intent);

        });

        aboutDeveloper.setOnClickListener(v -> {

            Intent intent =new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://about-developer-pdf-studio-web-page.netlify.app/"));
            startActivity(intent);

        });

        aboutPdfStudio.setOnClickListener(v -> {

            Intent intent =new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://pdfstudioapp.github.io/pdfstudio.github.io/about_pdf_studio.html"));
            startActivity(intent);

        });

        privacyPolicy.setOnClickListener(v -> {

            Intent intent=new Intent(requireContext(), PrivacyPolicy.class);
            startActivity(intent);

        });

        feedback.setOnClickListener(v -> {

            Intent intent=new Intent(requireContext(), FeedBack.class);
            startActivity(intent);

        });

        rateApp = requireActivity().findViewById(R.id.item_rate);

        RateAppSheetController  rateAppSheetController = new RateAppSheetController(view);

        rateApp.setOnClickListener(v -> {

            drawerLayout.closeDrawer(GravityCompat.START);

            rateAppSheetController.show();

        });




    }

    private void loadRecentFiles(View view) {

        LinearLayout container = view.findViewById(R.id.linearLayout_recent_files);
        container.removeAllViews();

        SharedPreferences prefs = requireContext().getSharedPreferences("files", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString("recent_files", null);

        Type type = new TypeToken<ArrayList<RecentFileItem>>(){}.getType();

        ArrayList<RecentFileItem> list;

        try {
            list = gson.fromJson(json, type);
        } catch (Exception e) {
            list = new ArrayList<>();

            prefs.edit().remove("recent_files").apply();
        }

        if (list == null) list = new ArrayList<>();

        int count = Math.min(2, list.size());

        if (count==0){
            no_history_img.setVisibility(View.VISIBLE);
            no_history_text.setVisibility(View.VISIBLE);
        }
        else{
            no_history_img.setVisibility(View.GONE);
            no_history_text.setVisibility(View.GONE);
        }



        for (int i = 0; i < count; i++) {
            int index=i;

            RecentFileItem file = list.get(i);

            Uri uri = Uri.parse(file.getPdfUri());

            View view_file = LayoutInflater.from(requireContext())
                    .inflate(R.layout.pdf_item_file, container, false);


            ((TextView) view_file.findViewById(R.id.pdfName)).setText(file.getPdfName());
            ((TextView) view_file.findViewById(R.id.pdfSize)).setText(file.getPdfSize());
            ((TextView) view_file.findViewById(R.id.pdfDate)).setText(file.getPdfDate());
            ((TextView) view_file.findViewById(R.id.pdfPages)).setText(file.getPdfPages()+" Pages");

            ImageView pdfView=view_file.findViewById(R.id.viewPDF);

            ImageView delete=view_file.findViewById(R.id.deletePDF);


            pdfView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), PdfViewerActivity.class);
                intent.putExtra("pdf_uri",uri);
                intent.putExtra("mode","view");
                v.getContext().startActivity(intent);
            });

            delete.setOnClickListener(v -> {

                DeletePDF.Remove_PDF_From_Storage(requireContext(),index);
                loadRecentFiles(view);


            });


            executors.execute(() ->{

                Bitmap firstPage= GetFirstPage.getFirstPageBitmap(requireContext(),uri);

                handler.post( () -> {

                    ((ImageView) view_file.findViewById(R.id.pagePreview)).setImageBitmap(firstPage);
                    ((TextView) view_file.findViewById(R.id.textPDF)).setVisibility(View.VISIBLE);

                });

            });

            container.addView(view_file);
        }
    }

    private boolean isUserLoggedIn() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        return  user == null ? false : true;

    }




    public interface  homeActionListener{

        void  onProfileClicked();
        void  onNotificationClicked();
        void  onSearchClicked();

        void  onLoginClicked();

    }


}