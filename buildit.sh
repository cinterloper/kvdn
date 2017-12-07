docker run -t -i -v $(realpath ~/.m2):/root/.m2 -e GITHUB_TOKEN cinterloper/lash bash -c 'cd /mnt/; bats workflow/build.bats; bats workflow/publish.bats; bats workflow/clean.bats'

